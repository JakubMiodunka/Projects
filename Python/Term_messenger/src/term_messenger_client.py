import concurrent.futures
import datetime
import json
import logging
import socket


class Message:
    """
        Reprosentation of one single message posted on server with all details about it.
        Instances of this calss is used as elements of ClientSocket.messages list.
    """

    def __init__(self, serialized: str, delimeter: str) -> None:
        deserialized = serialized.split(delimeter)  #Deserialization of UPDATE_RESP content
        
        #Properties init
        self.sender = deserialized[0]                                   #Message sender nickname
        iso_timestamp = deserialized[1]                                 #Timestamp when message was posted on server
        self.timestamp = datetime.datetime.fromisoformat(iso_timestamp)
        self.content = deserialized[2]                                  #Content of the message


class ClientSocket:
    def __init__(self, HOST: str, PORT:int, logger: logging.Logger) -> None:
        #Properties init
        self.HOST = HOST                #Server host
        self.PORT = PORT                #Server port number
        #TBD - advanced host and port validation

        self.socket = socket.socket()   #Socket creation
        self.socket.settimeout(1)       #Socket timeout set to 1s to prevent stucking in recive thread - see more in ClientSocket.recive method
        self.connected = False          #Property set to True if connection to the server will be established, and set to False if lost or not established yet
        self.authenticated = False      #Property set to True if client is concidered as authenticated by the server

        self.recived = dict()           #Buffer between self.recive and self.sent methods
        self.messages = list()          #List of messages posted on the server in chronological order.
                                        #Updated by self.update_by_UPDATE_REQ. Elements of the list are Message type objects.
        self.next_message_id = 0        #Used for message ID generation that will be added to newly sended messages

        self.logger = logger            #Internal logger

        #Logging message
        self.logger.info("Client socket successfully initalized")

    def begin(self) -> bool:
        #Interupt if socket is already connected
        if self.connected:
            self.logger.warning("Connection request on connected socket occur")
            return self.connected

        #Connectingg socket to the server
        try:
            self.socket.connect((self.HOST, self.PORT))
            self.connected = True
            self.logger.info(f"Main socket successfully connected to {self.HOST}:{self.PORT}")

        except ConnectionRefusedError:
            self.logger.critical(f"Failed to connect to {self.HOST}:{self.PORT}")

        return self.connected

    def recive(self) -> None:
        #Checking if socket is propertly opend
        if not self.connected:
            self.logger.warning("Receiving attempt on not connected socket occurred")
            return

        try:
            self.logger.info("Listening for new messages")

            while self.connected:
                #Waiting for new message
                try:
                    raw_data = self.socket.recv(1024)
                except socket.timeout:
                    #Catching socket.timeout combined with 1s socket timeout results in behaviour when
                    #once per 1s method will check if socket is still connected.
                    continue
                
                #Checking if server closed its socket
                if not raw_data:
                    self.logger.error(f"Server closed its socket")
                    self.close()
                    return

                #Decoding recived data
                decoded_data = raw_data.decode("utf-8")
                
                #Transforming recived data to dict
                try:
                    data_dict = json.loads(decoded_data)

                except json.JSONDecodeError:
                    self.logger.error(f"Invalid data recived - data not in Json format {raw_data} - closing connection")
                    self.close()
                    return

                #Recived data validation
                if self._validate_recived_dict(data_dict):
                    self.logger.debug(f"Recived {data_dict['type']} with following data: {data_dict['content']}")

                    #Adding recived data to list
                    message_id = data_dict["id"]
                    self.recived[message_id] = data_dict
                else:
                    self.logger.error(f"Recived data in invalid format - closing connection")
                    self.close()
                    return

        except BrokenPipeError:
            self.logger.critical(f"Connection to server lost")
            self.close()

    def _sent(self, data_dict: dict) -> dict:
        #Checking if socket is propertly opend
        if not self.connected:
            self.logger.warning("Sending attempt on not connected socket occurred")
            return dict()

        #Adding message ID to data
        message_id = self._generate_message_id()
        data_dict["id"] = message_id

        #Transforming data to required format
        data_json = json.dumps(data_dict)
        data_raw = data_json.encode("utf-8")

        #Seinding data through the socket
        try:
            self.logger.debug(f"Sending {data_dict['type']} with following content: {data_dict['content']}")
            self.socket.sendall(data_raw)

        except BrokenPipeError:
            self.logger.error(f"Connection to server lost")
            self.close()
            return dict()

        #Waiting for response
        self.logger.debug("Waiting for response")
        while self.connected:
            if message_id in self.recived:
                return self.recived.pop(message_id)

    def sent_CONN_REQ(self, nickname: str) -> bool:
        #Checking if client is already authenticated
        if self.authenticated:
            self.logger.warning("Reauthentication attempt occured")
            return False

        #Logging message
        self.logger.debug(f"Attempting to authenticate as '{nickname}'")

        #Message generation
        message = {"type": "CONNECT_REQ", "content": nickname}
        
        #Sending and waiting for response
        response = self._sent(message)

        #Checking if returned dict is not empty - it means that something went wrong in self._sent method
        if not response:
            return False

        #Response check
        result = response["content"]
        if result == "accepted":
            self.logger.info("Client successfully authenticated")
            self.authenticated = True
            return True
        else:
            self.logger.warning("Authentication failed")
            return False

    def sent_POST_REQ(self, message_content: str) -> bool:
        #Checking if client is authenticated
        if not self.authenticated:
            self.logger.warning("Failed to sent POST_REQ - client is not authenticated")
            return False

        #Logging message
        self.logger.debug(f"Attempting to post following message: {message_content}")

        #Message generation
        message = {"type": "POST_REQ", "content": message_content}
        
        #Sending and waiting for response
        response = self._sent(message)

        #Checking if returned dict is not empty - it means that something went wrong in self._sent method
        if response:
            self.logger.debug("Message successfully posted on server")
            return True
        else:
            self.logger.warning("Failed to post message on server")
            return False

    def update_by_UPDATE_REQ(self) -> bool:
        #Checking if client is authenticated
        if not self.authenticated:
            self.logger.warning("Failed to sent UPDATE_REQ - client is not authenticated")
            return False

        #Logging message
        self.logger.debug("Attempting to update message list")

        message_counter = 0
        while True:
            #Checking if message list is empty to determine timestamp that will be sent in UPDATE_REQ
            if not self.messages:
                min_year = datetime.MINYEAR
                timestamp = datetime.datetime(min_year, 1, 1)
            else:
                timestamp = self.messages[-1].timestamp

            #Message generation
            message = {"type": "UPDATE_REQ", "content": timestamp.isoformat()}

            #Sending and waiting for response
            response = self._sent(message)

            #Checking if returned dict is not empty - it means that something went wrong in self._sent method
            if not response:
                self.logger.debug("Failed to update message list")
                return False

            #Checking if clients messag elist is up to date
            if response["content"] == "":
                self.logger.debug(f"Message list fully updated - new messages: {message_counter}")
                return True
            else:
                #adding new message to message list
                new_message = Message(response["content"], "#")
                self.messages.append(new_message)

                #Counter incementations
                message_counter += 1

    def close(self) -> None:
        self.socket.close()
        self.connected = False
        self.authenticated = False

    @staticmethod
    def _validate_recived_dict(recived_dict: dict) -> bool:

        try:
            #Length validation
            assert len(recived_dict) == 3
            
            #Message ID validation
            assert "id" in recived_dict
            assert isinstance(recived_dict["id"], int)

            #Type validation
            assert "type" in recived_dict
            assert recived_dict["type"] in ["UPDATE_RESP", "POST_RESP", "CONNECT_RESP"]

            #Content validation
            assert "content" in recived_dict
            assert isinstance(recived_dict["content"], str)

            #Validationg timestamp in UPDATE_RESP
            if recived_dict["type"] == "UPDATE_RESP":
                if recived_dict["content"] != "":
                    message_content = recived_dict["content"].split("#")
                    timestamp = message_content[1]
                    datetime.datetime.fromisoformat(timestamp)

            return True

        except AssertionError:  #Exception raised by assertions
            return False

        except ValueError:      #Exception raised by datetime.datetime.fromisoformat()
            return False

    def _generate_message_id(self) -> int:
        #Checking if self.next_message_id fits in range. If not it will be set to zero.
        if self.next_message_id == 1000:
            self.next_message_id = 0

        #Incrementation of self.next_message_id
        self.next_message_id += 1

        #Returning resault
        return self.next_message_id


if __name__ == "__main__":
    #Logger config
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)

    stream_handler = logging.StreamHandler()
    formatter = logging.Formatter("[%(levelname)s][%(asctime)s] %(message)s")
    stream_handler.setFormatter(formatter)

    logger.addHandler(stream_handler)
    while True:
        HOST = input("Host: ")
        PORT = input("Port: ")

        client = ClientSocket(HOST, int(PORT), logger)
        if client.begin():
            break
    
    print("connected")

    with concurrent.futures.ThreadPoolExecutor() as executor:
        executor.submit(client.recive)

        while True:
            nickname = input("Nickname: ")
            if client.sent_CONN_REQ(nickname):
                break
        print("authenticated")
        client.close()





        

