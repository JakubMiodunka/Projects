import argparse
import json
import logging
import pathlib
import time
import watchdog.events
import watchdog.observers


######################################
#       Author: Jakub Miodunka       #
#            Version: 4              #
#       Last update: 09.01.2023      #
#   Used version of Python: 3.8.10   #
######################################


class DirManager(watchdog.events.FileSystemEventHandler):
    """
        Custom file system event handler 
    """

    def __init__(self, root: pathlib.Path, config: dict, logger: logging.Logger) -> None:
        # Arguments validation
        if not root.exists(): raise FileNotFoundError("Given root directory does not exist")
        if not root.is_dir(): raise NotADirectoryError("Given root is not a directory")
        self.validate_config(config)

        # Calling parent __init__
        super().__init__()
        
        # Properties init
        self.root = root.absolute()                     # Absolute path to dir that will be menaged.
        self.structure = self.structure_init(config)    # Desired structure of menaged dir. Structure: {<File extension: str>: <Subdir absolute path: pathlib.Path>}
        self.subdirs = set(self.structure.values())     # Uniq paths to subdirs from self.structure
        self.brakedown_mode = False                     # Activation of brakedown mode blocks any actions from DirManager
        self.logger = logger                            # logging.Logger instance that will be used by class instance

        # Logging
        self.logger.info(f"Init completed - '{self.root}' set as a root directory")
    
    @staticmethod
    def validate_config(config: dict) -> None:
        """
            Performs validation of given config.
            If it is not valid according exception will be raised.
        """

        for extension, subdir_name in config.items():
            # Extension validation
            if not isinstance(extension, str): raise TypeError("Type of extension provided in config is not str")
            if not extension.startswith("."): raise ValueError("Extension provided in config do not start with a dot")
            if not  extension.count(".") == 1: raise ValueError("Extension provided in config contains more than one dot")
            if pathlib.os.sep in extension: raise ValueError("Extension provided in config contains os separator")

            # Subdir name validation
            if not isinstance(subdir_name, str): raise TypeError("Type of subdir provided in config is not str")
            if pathlib.os.sep in subdir_name: raise ValueError("Subdir provided in config contains os separator")
            if "."  in subdir_name: raise ValueError("Subdir provided in config contains a dot")

    def structure_init(self, config: dict) -> dict:
        """
            Supplementary method used for initialisation of self.structure.
        """

        # Creation of structure dict
        structure = dict()

        # Filling up created dict with file extensions as keys and absolute paths to subdirs as values
        for extension, subdir in config.items():
            structure[extension] = self.root / subdir

        # Returning generated structure
        return structure

    def static_management(self) -> None:
        """
            Static (non event related) root directory organisation according to convention given in self.structure.
            Should be called before starting watchdog.observers.Observer thread to ensure cohesion of root directory
            before handling any file system event.
        """

        # Logging
        self.logger.debug(f"Static mamagement of '{self.root}' begin")

        # Looping over subdirs
        for subdir in self.subdirs:
            # Ensuring that subdir exist in data system
            if not subdir.exists():
                # If not it will be created
                subdir.mkdir()

                # Logging
                self.logger.info(f"Subdir '{str(subdir)}' created")
            else:
                # If it already exist elements stored in it will be checked if they follow convention
                # stored in self.structure and moved if required
                for element in subdir.iterdir():
                    if self.action_required(element):
                        self.move(element)

        # Checking root directory for any elements that needs to be moved to math convention given in self.structure
        for element in self.root.iterdir():
            # Ignoring subirs
            if element in self.subdirs:
                continue
                
            # Checkig rest of the elements
            if self.action_required(element):
                self.move(element)

        # Logging
        self.logger.debug(f"Static mamagement of '{self.root}' finished")

    def on_created(self, event) -> None:
        """
            Overwrite of watchdog.events.FileSystemEventHandler.on_created method.
            Called every time when new element is created in root directory.
        """

        # Checking if brakedown mode is activated
        if self.brakedown_mode:
            return

        # Extracting source path from event
        trigger = pathlib.Path(event.src_path)
        
        # Checking if element that triggered the event follows convetnion stored in self.structure
        if self.action_required(trigger):
            # Moving element to desired location if needed
            self.move(trigger)

    def on_deleted(self, event) -> None:
        """
            Overwrite of watchdog.events.FileSystemEventHandler.on_deleted method.
            Called every time when any element in root directory is deleted.
        """

        # Checking if brakedown mode is activated
        if self.brakedown_mode:
            return

        # Extracting source path from event
        src = pathlib.Path(event.src_path)

        # Checking if root directory was not delated
        if src == self.root:
            # Logging
            self.logger.critical("Root directory was delated - brakedown mode activated")
            
            # Activating brakedown mode
            self.brakedown_mode = True

            # Exiting
            return

        # If source of event will be one of the subdirs mentioned in self.structure it will be recreated
        if src in self.subdirs:
            # Subdir recreation
            src.mkdir()

            #Logging
            self.logger.debug(f"Recreation of '{src}' subdir as it was deleted")

    def on_moved(self, event) -> None:
        """
            Overwrite of watchdog.events.FileSystemEventHandler.on_moved method.
            Called every time when any element in root directory is moved or renamed.
        """
        
        # Checking if brakedown mode is activated
        if self.brakedown_mode:
            return

        # Extracting paths from event
        src = pathlib.Path(event.src_path)
        dest = pathlib.Path(event.dest_path)

        # Checking if one of subdirs was moevd or renamed and recreation of it if needed
        if src in self.subdirs:
            # Subdir recreation
            src.mkdir()

            # Logging
            self.logger.info(f"Recreation of '{src}' subdir as it was moved or renamed")

            # Exiting
            return

        # Checking if element that triggered the event follows convetnion stored in self.structure
        if self.action_required(dest):
            # Moving element to desired location if needed
            self.move(dest)

    def action_required(self, element: pathlib.Path) -> bool:
        """
            Method checks if given element requires some actions from DirManager.
            It is also activcating brakedown mode if given element is placed in root directory or direclty in one of subdirs but is not file or directory.

            Returns 'True' as indocator that element is not fulfiling convention stored in self.structure and some actions related to it are required,
            'False' otherwise.
        """

        # Logging
        self.logger.debug(f"Analysing '{element}'")

        # If element is not placed in root or directly in one of subdirs action is not required
        if element.parent != self.root and element.parent not in self.subdirs:
            # Logging
            self.logger.debug(f"'{element}' is not placed in root or in one of subdirs")
            # Returning false to prevent any actions related to element
            return False

        # Validating if element is in localisation that follows self.structure
        try:
            # If element is a direcotry it hould be placed in root
            if element.is_dir():
                assert element.parent == self.root

            # If element is a file it should be placed in one of subdirs or in root depending on self.structure
            if element.is_file():
                assert element.parent == self.structure.get(element.suffix, self.root)

            # Logging
            self.logger.debug(f"'{element}' follows convention.")

            # If above cinditions will be fulfiled 'False' will be returned as indicator that actions realted to element is not required
            return False

        except AssertionError:
            # Logging
            self.logger.debug(f"'{element}' do not follow convention.")

            # Returning 'True' as idicator that element should be moved to other location
            return True

    def move(self, source: pathlib.Path) -> None:
        """
            Method is moving given element to location that follows convention stored in self.structure.
            If destination path already exists prefix in format '_<int>' will be added to file name to prevent overwrite.
        """

        # Logging
        self.logger.debug(f"Moving '{source}'")

        # Checking if element is a file or directory - performing actions on other type of element is not allowed
        if not (source.is_dir() or source.is_file()):
            # Logging
            self.logger.error("Cannot move '{source}' as it is not a directory nor a file - move request dropped")
            return

        # Genrating destination path according to self.structure
        destination = self.structure.get(source.suffix, self.root) / source.name

        # If some other element exist under generated destination path suffix to its name will be added to
        # prevent overwritting

        #Logging
        if destination.exists():
            self.logger.debug(f"As '{destination}' already exist suffix to filename will be added")

        # Adding suffix to file name
        suffix = 0
        while destination.exists():
            suffix += 1
            destination = self.structure.get(source.suffix, self.root) / f"{source.stem}_{suffix}{source.suffix}"

        #Creating a copy of source path in str format for logging purposes
        source_copy = str(source)

        try:
            # Moving given element to desired location
            source.rename(destination)

            # Logging
            self.logger.info(f"'{source_copy}' moved to '{destination}'")
            
        except PermissionError:     # Usually raised when cannot access the element because it is being used by another process
            # Logging
            self.logger.error(f"Cannot move '{source}' due to permission error - move request dropped")
            return


# Main part of the program
if __name__ == "__main__":
    # Parsing arguments
    parser = argparse.ArgumentParser(description="Watchdog script that will ensure that structure of menaged directory follows convention given in config file.")
    parser.add_argument("root", help="Path to directory that will be managed")
    parser.add_argument("config", help="Path to *.json file with config")
    parser.add_argument("-d", "--debug", required=False, action="store_true", help="Debug mode - more verbose console messages")
    parser.add_argument("-s", "--silent", required=False, action="store_true", help="Silent mode - disables console messages")
    parser.add_argument("-l", "--log", required=False, help="Path to log file. If file already exists it will be overwritten. Messages logged into file will be verbosed." )
    args = parser.parse_args()

    # Creating pathlib.Path objects related to given arguments
    config_file = pathlib.Path(args.config).absolute()
    root =  pathlib.Path(args.root).absolute()
    log_file = pathlib.Path(args.log).absolute() if args.log else None

    # Arguments validation
    if not config_file.exists(): raise FileNotFoundError("Given config file does not exist")
    if not config_file.is_file(): raise IsADirectoryError("Given config is not a file")
    if not config_file.suffix == ".json": raise ValueError("Given config is not *.json file")

    if log_file:
        if not log_file.parent.exists(): raise FileNotFoundError("Parent directory of given log file does not exist")

    # Preparing logger
    logger = logging.getLogger(__name__)
    
    # Setting logger level to DEBUG
    logger.setLevel(logging.DEBUG)

    # Creating formatter for message handlers
    formatter = logging.Formatter("[%(levelname)s][%(asctime)s] %(message)s")

    # Setting up stream handler if silent mode was not activated
    if not args.silent:
        # Creating stream handler
        stream_handler = logging.StreamHandler()

        # Setting logging level according to given arguments
        if not args.debug:
            stream_handler.setLevel(logging.INFO)
        
        # Adding message formatter to the handler
        stream_handler.setFormatter(formatter)

        # Adding handler to the logger
        logger.addHandler(stream_handler)

    # Setting up file handler if logging to file was enabled
    if log_file:
        # Creating file handler
        # Note that mode is set to 'w' which means that given file will be overwritten if it exists.
        file_handler = logging.FileHandler(log_file, mode='w')

        # Adding message formatter to the handler
        file_handler.setFormatter(formatter)

        # Adding handler to the logger
        logger.addHandler(file_handler)
    
    # Creating event handler
    with open(config_file) as file:
        config = json.load(file)

    event_handler = DirManager(root, config, logger)
    
    # Initialising observer
    observer = watchdog.observers.Observer()
    observer.schedule(event_handler, root, recursive=True)
    
    # Launching initial static (non event related) root dir organisation
    event_handler.static_management()

    # Starting observer thread
    observer.start()

    # Doing nothing as all program functionalities are handled by observer and enent handler in sepparate thread
    try:
        while True:
            time.sleep(3600)
    except KeyboardInterrupt:
        # Shutting down observer thread
        observer.stop()
        observer.join()