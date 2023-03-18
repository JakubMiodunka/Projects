import concurrent.futures
import datetime
import json
import logging
import socket

#############################
#   Author: Jakub Miodunka  #
#   Version: 1.0            #
#   Last update: 06.10.2022 #
#############################

#Classes
class Message:
    """
        Reprosentation of one single message posted on server with all details about it.
        Instances of this calss is used as elements of Server.__messages list.
    """

    def __init__(self, sender: str, content: str) -> None:
        self.sender = sender                        #Message sender nickname
        self.timestamp = datetime.datetime.now()    #Timestamp when message was posted on server
        self.content = content                      #Content of the message


class Server:
    """
        Class resposnible for creating server on given address (host and port) and handling incomming connections.
        Connections are handled in parallel using threading.
        There is also implemented logging mechanism basing on logging module and logging.Logger type object.

        Logging levels convention:
            INFO - basic informations about actions taking place on server
            DEBUG - more detailed informations
            WARNING - used for reporting security issues
            ERROR - issues with recived data resulting in client disconnection
            CRITICAL - issues related to the server itself, critical to its functioning

        Data sended to the server shoud be a Json formatted string that can be converted to dict using json.dumps() method.
        That kind of dict should have follwoing structure:
            {"type":        <type of message: str>,
             "content":    <content of the message: str>}

        Types of messages:
            1. CONNECT_REQ
               It should be the first message sent to the server by new client.
               Client specyfies in content of this message his/her nickname that will be visable for other users.
               After successfull nickname assign client is treated as authenticated.

               Message structure:
                    {"id":       <message ID: int>,
                     "type":     "CONNECT_REQ",
                     "content":  <desired nickname: str>}

               Response:
                    {"id":       <CONNECT_REQ message ID: int>,
                     "type":    "CONN_RESP",
                     "content": <"accepted" or "rejected">}

                    If the content of the response is "rejectred" that means that nickname desired by user is already used by other client.
                    Authentication fails and client must sent another CONNECT_REQ.

            2. POST_REQ
               This type of message should be sent by client if he/she want to send something to the server - content of this message along
               with the authors nickname will be available to other users.

               Message structure:
                    {"id":       <message ID: int>,
                     "type":     "POST_REQ",
                     "content":  <message: str>}

                    In content of the message char '#' is prohibited - it is reserved as delimeter by UPDATE_RESP.

               Response:
                    {"id":     <POST_REQ message ID: int>,
                    "type":    "POST_RESP",
                     "content": ""}

            3. UPDATE_REQ
                It should be used when client whats to be updated about message recived by server from other users.
                
                Message structure:
                    {"id":       <message ID: int>,
                     "type":     "UPDATE_REQ",
                     "content":  <timestamp in ISO format: str>}

                Response:
                    {"id":     <UPDATE_REQ message ID: int>,
                     "type":    "UPDATE_RESP",
                     "content": "<message author>#<ISO format timestamp when message was recived by server>#<message content>"}

                    In response to UPDATE_REQ are details about first message recived by server after timestamp given in UPDATE_REQ.
                    If there is no newer messages than timestamp given in UPDATE_REQ content of the message will be empty string.
                    In UPDATE_RESP '#' is used as delimeter.

        If any of rules related to the messages format will be broken by client it will be disconnected from the server.
    """

    def __init__(self, host: str, port: int, logger: logging.Logger, max_stored_messages: int = 20) -> None:
        """
            Constructor perform only properties init.
                Arguments:
                    host - host that can be accepted by socket.bind() method
                    port - host that can be accepted by socket.bind() method
                    logger - logging.Logger type object that will be used for logging pourpouses.
                             None of the moethods from this class does not perform nothing more than logging on this logger - it should be configured externally.
        """

        #Properties init
        self.logger = logger

        self.HOST = host
        self.PORT = port
        #TBD - advanced host and port validation

        self.active_connections = 0
        self.users = dict()     #Dict that contains mapping between user ID and its nickname.
                                #Users that are contained by this dict are considered as authenticated.
                                #Dict structure: {<user ID: str>: <nickname: str>}
        self.messages = list()  #List of Message type objects that contains all messages posterd on server.
        self.max_stored_messages = max_stored_messages  #Maximum lenth of self.messages

        #Logging message
        self.logger.info("Server successfully initialized")

    def begin(self) -> None:
        """
            Main method of the server type object.
            Creating main server socket, accepting incoming connections and starting new threads takes place here.
        """

        #Server socket creation
        with socket.socket() as main_socket:
            #Setting additional options of the socket to prevent 'OSError: [Errno 98] Address already in use' error
            main_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

            #Binding socket to requested HOST and PORT
            try:
                main_socket.bind((self.HOST, self.PORT))
            except OSError:
                self.logger.critical("Failed to bind socket to specyfied address")
                raise

            #Waiting for connections
            main_socket.listen()
            self.logger.info("Socket successfully created - wiating for connections")

            #Handling incoming connections
            with concurrent.futures.ThreadPoolExecutor() as executor:
                while True:
                    #Waiting for new connection
                    conn_socket, conn_address = main_socket.accept()

                    #Launching new thread to handle new connection
                    executor.submit(self._handle_client, conn_socket, conn_address)

    def _handle_client(self, conn_socket: socket.socket, conn_address: tuple) -> None:
        """
            Method that is started for each client in seppareate thread.
            It is repsonsible to iterpreting messages recived from client and responding to it.
            If data recived from client will be invalid in any scope client will be disconnected.
                Arguments:
                    conn_socket - socket returned by socket.accept()
                    conn_address - addres returned by socket.accept()
        """

        #User ID generation
        CONN_HOST, CONN_PORT = conn_address
        USER_ID = f"{CONN_HOST}:{CONN_PORT}"
        
        #Properties update
        self.logger.info(f"New connection from {USER_ID}")
        self.active_connections += 1
        self.logger.debug(f"Active connctions: {self.active_connections}")

        #Handling given connection
        with conn_socket:
            while True:
                #Waiting for incoming data
                raw_data = conn_socket.recv(1024)

                #Checking if conneciton was closed
                if not raw_data:
                    self.logger.info(f"Closing connection with {USER_ID}")
                    
                    #Removing user form users list if neccessary
                    self._remove_from_user_list(USER_ID)

                    #Closing connection with client
                    break
                
                #Decoding recived data
                decoded_data = raw_data.decode("utf-8")
                
                #Transforming recived data to dict
                try:
                    data_dict = json.loads(decoded_data)

                except json.JSONDecodeError:
                    self.logger.error(f"Closing connection with {USER_ID} due to invalid data recived - data not in Json format")
                    
                    #Removing user form users list if neccessary
                    self._remove_from_user_list(USER_ID)

                    #Closing connection with client
                    break
                
                #Recived data validation
                if self._validate_recived_dict(data_dict):
                    #Logging message
                    self.logger.debug(f"From {USER_ID} recived {data_dict['type']} with follwoing content: '{data_dict['content']}'")
                    
                    #Checking if user is authenticated
                    if data_dict["type"] in ["UPDATE_REQ", "POST_REQ"]:
                        if USER_ID not in self.users:
                            self.logger.warning(f"Closing connection with {USER_ID} due to detected trial of performing restricted action from user that is not authenticated")
                            
                            #Closing connection with client
                            break

                    #Respond generation based on recived data type
                    if data_dict["type"] == "UPDATE_REQ":
                        response_dict = self._respond_with_UPDATE_RESP(data_dict["content"], data_dict["id"], USER_ID)

                    elif data_dict["type"] == "POST_REQ":
                        response_dict = self._respond_with_POST_RESP(data_dict["content"], data_dict["id"], USER_ID)

                    elif data_dict["type"] == "CONNECT_REQ":
                        response_dict = self._respond_with_CONNECT_RESP(data_dict["content"], data_dict["id"], USER_ID)

                    #Transforming response returned by handler methods to required format
                    response_json = json.dumps(response_dict)
                    response_raw = response_json.encode("utf-8")

                    #Sending response
                    conn_socket.sendall(response_raw)
                else:
                    self.logger.error(f"Closing connection with {USER_ID} due to invalid data format")
                    
                    #Removing user form users list if neccessary
                    self._remove_from_user_list(USER_ID)
                    
                    #Closing connection with client
                    break
                
        #Properties update
        self.active_connections -= 1
        self.logger.debug(f"Active connctions: {self.active_connections}")

    @staticmethod
    def _validate_recived_dict(recived_dict: dict) -> bool:
        """
            Method implements alidation of inciming messages.
                Arguments:
                    recived_dict - message in dict format (after conversion from string in Json format to dict)
                Return value:
                    True or False depending on result of validation
        """

        try:
            #Length validation
            assert len(recived_dict) == 3
            
            #Message ID validation
            assert "id" in recived_dict
            assert isinstance(recived_dict["id"], int)

            #Type validation
            assert "type" in recived_dict
            assert recived_dict["type"] in ["UPDATE_REQ", "POST_REQ", "CONNECT_REQ"]

            #Content validation
            assert "content" in recived_dict
            assert isinstance(recived_dict["content"], str)

            #Validationg timestamp in UPDATE_REQ
            if recived_dict["type"] == "UPDATE_REQ":
                datetime.datetime.fromisoformat(recived_dict["content"])

            #Content validation in POST_REQ
            if recived_dict["type"] == "POST_REQ":
                assert "#" not in recived_dict["content"]

            return True

        except AssertionError:  #Exception raised by assertions
            return False

        except ValueError:      #Exception raised by datetime.datetime.fromisoformat()
            return False

    def _remove_from_user_list(self, user_id: str) -> None:
        """
            Method removes user with given user ID from users list.
            If user is not mentioned in users list no exception will be raised.
        """

        if user_id in self.users:
            self.logger.debug(f"{user_id} removed from users list")
            self.users.pop(user_id)

    #Respond handlers
    def _respond_with_UPDATE_RESP(self, iso_timestamp: str, message_id: int, user_id: str) -> dict:
        """
            Method used for generation respond to UPDATE_REQ.
                Arguments:
                    iso_timestamp - timestamp in ISO format from UPDATE_REQ content
                    user_id - user ID in format '<HOST>:<PORT>'
                Return value:
                    dict that can be converted to bytes and send back to client as response
        """

        #Converting given ISO timestamp to datetime.datetime object
        timestamp = datetime.datetime.fromisoformat(iso_timestamp)

        #Searching for message newer than given timestamp
        for index, message in enumerate(self.messages):
            if message.timestamp > timestamp:
                #Logging message
                update_progress = (index + 1)/len(self.messages) * 100
                self.logger.debug(f"{user_id} updates its message list - progress {index + 1}/{len(self.messages)} ({round(update_progress)}%)")

                #Generating response containing details about found message
                return {"id": message_id, "type": "UPDATE_RESP", "content": f"{message.sender}#{message.timestamp.isoformat()}#{message.content}"}
            
        self.logger.debug(f"{user_id} is up to date with message list")
        
        #Returning response with empty content
        return {"id": message_id, "type": "UPDATE_RESP", "content": ""}

    def _respond_with_POST_RESP(self, message_conttent: str, message_id: int, user_id: str) -> dict:
        """
            Method used for generation respond to POST_REQ and adding recived messages to message list property.
            It is also responsible for menaging the size of message list.
                Arguments:
                    message_conttent - content of recived POST_REQ
                    user_id - user ID in format '<HOST>:<PORT>'
                Return value:
                    dict that can be converted to bytes and send back to client as response
        """

        #Menaging the size of message list
        if len(self.messages) == self.max_stored_messages:
            self.messages.pop(0)
            self.logger.debug("Too many messages in message list - oldest message was removed")
        
        #Adding new message to message list
        sender_nickname = self.users[user_id]
        new_message = Message(f"{sender_nickname}", message_conttent)
        self.messages.append(new_message)

        self.logger.debug(f"Message from {user_id} added to message list. Size of message list: {len(self.messages)}")
        
        #Generating response
        return {"id": message_id, "type": "POST_RESP", "content": ""}

    def _respond_with_CONNECT_RESP(self, nickname: str, message_id: int, user_id: str) -> dict:
        """
            Method used for generation respond to CONNET_REQ.
            It is also adding users to user list property (authenticate them)
            If content of the generated message poiting at rejection it means that nickane desired by client is already used by other client.
                Arguments:
                    nickname - content of recived CONNECT_REQ that represents nickenme desired by user
                    user_id - user ID in format '<HOST>:<PORT>'
                Return value:
                    Dict that can be converted to bytes and send back to client as response.
        """
        if nickname not in self.users.values():
            self.users[user_id] = nickname
            self.logger.info(f"Sender {user_id} successfully added to users list as '{nickname}'")
            return {"id": message_id, "type": "CONNECT_RESP", "content": "accepted"}
        else:
            self.logger.debug(f"Fail to add {user_id} to users list - nickname '{nickname}' already exists on the list.")
            return {"id": message_id, "type": "CONNECT_RESP", "content": "rejected"}


#Main part of the program
if __name__ == "__main__":
    #Definig contants
    HOST = "127.0.0.1"
    PORT = 65432

    #Logger config
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)

    stream_handler = logging.StreamHandler()
    formatter = logging.Formatter("[%(levelname)s][%(asctime)s] %(message)s")
    stream_handler.setFormatter(formatter)

    logger.addHandler(stream_handler)

    #Server launch
    echo_server = Server("127.0.0.1", 65432, logger)
    echo_server.begin()