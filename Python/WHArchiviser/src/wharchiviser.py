import curses
import curses.textpad as textpad
import datetime
import json
import os

######################################
#       Author: Jakub Miodunka       #
#             Version: 2             #
#       Last update: 10.07.2022      #
#   Used version of Python: 3.8.10   #
######################################

#Classes
class ArchiveMenager:
    """
    Class where are stored methods desired to menage files in created archive.
        Required modules:
           datetime
           json
           os
    """

    #Class methods
    @classmethod
    def LoadFromJson(cls, companyName: str, date: datetime.date) -> dict:
        """"
        Method loads data from *.josn file that was searched in archive based on given date and company name.
            Arguments:
                date, companyName - arguments required for searching desired *.json file
            Return valuse:
                Dict that was stored in *.json file or 'None' if required file does not exists.
        """
        #Searching requested *.json file in archive
        jsonFilePath = cls.CheckIfJsonExists(companyName, date)

        #Returning *.json file content as dict or returning 'None' if it not exists
        if jsonFilePath is not None:
            with open(jsonFilePath, "r") as jsonFile:
                return json.load(jsonFile)
        else:
            return None

    @classmethod
    def RemoveJson(cls, companyName: str, date: datetime.date) -> None:
        """
        Method will remove *.json file corresponding to given arguments and corresponding dirs to it if necessary.
            Arguments:
                date, companyName - arguments required for searching desired *.json file
        """
        #Searching requested *.json file in archive
        jsonFilePath = cls.CheckIfJsonExists(companyName, date)

        if  jsonFilePath is not None:
            #Removing file if it exists in archive
            os.remove(jsonFilePath)

            #Checking if <home dir>/Archive/<company name>/<year> dir is empty. If it is this dir will be removed.
            yearDirPath = os.path.dirname(jsonFilePath)
            if len(os.listdir(yearDirPath)) == 0:
                os.rmdir(yearDirPath)

            #Checking if <home dir>/Archive/<company name> dir is empty. If it is this dir will be removed.
            archiveDirPath = os.path.dirname(yearDirPath)
            if len(os.listdir(archiveDirPath)) == 0:
                os.rmdir(archiveDirPath)

    #Static methods
    @staticmethod
    def CheckIfJsonExists(companyName: str, date: datetime.date) -> str:
        """
        Method checks if *.json file representing day given as datetime.date object exists in archive.
            Return values:
                Path to file or 'None' depending on existance of requested *.json file.
        """
        
        #Generating path to requested *.json file
        homeDirPath = os.path.dirname(__file__)
        jsonFilePath = os.path.join(homeDirPath, "Archive", companyName, str(date.year), f"{date.isoformat()}.json")

        #Checking if file exists
        if os.path.exists(jsonFilePath):
            return jsonFilePath
        else:
            return None

    @staticmethod
    def GetCompanies() -> list:
        """
        Method returns list of comapanies that are already present in archive.
        Elemnts in list are in string format.
        """
        #Generating required paths
        homeDirPath    = os.path.dirname(__file__)               #<home dir>/
        archiveDirPath = os.path.join(homeDirPath, "Archive")    #<home dir>/Archive

        #If archive exists list of comapnies wil be return. If not empty list will be returned
        if os.path.exists(archiveDirPath):
            return os.listdir(archiveDirPath)
        
        return list()

    @staticmethod
    def SaveToJson(companyName: str, date: datetime.date, daySummary: dict) -> None:
        """
        Method saves given dict to *.json file in localisation that matches archivising convention.
            Arguments:
                companyName, date - arguments required to generate correct path for *.json file
                daySummary - that dict will be saved in *.josn file
        """
        #Generating required paths
        homeDirPath    = os.path.dirname(__file__)                                #<home dir>/
        archiveDirPath = os.path.join(homeDirPath, "Archive")                     #<home dir>/Archive
        companyDirPath = os.path.join(archiveDirPath, companyName)                #<home dir>/Archive/<company name>
        yearDirPath    = os.path.join(companyDirPath, str(date.year))             #<home dir>/Archive/<company name>/<year>
        jsonFilePath   = os.path.join(yearDirPath, f"{date.isoformat()}.json")    #<home dir>/Archive/<company name>/<year>/<date in format YYYY-MM-DD>.json

        #If direct path to <home dir>/Archive/<company name>/<year> does not exist, it will be created
        if not os.path.exists(yearDirPath):
            #Checking if <home dir>/Archive exists ,if not 'Archive' dir it will be created
            if not os.path.exists(archiveDirPath):
                os.mkdir(archiveDirPath)

            #Checking if <home dir>/Archive/<company name> exists, if not dir named accordingly to provided company name will be created
            if not os.path.exists(companyDirPath):
                os.mkdir(companyDirPath)

            #Checking if <home dir>/Archive/<year> exists, if not year dir will be created
            if not os.path.exists(yearDirPath):
                os.mkdir(yearDirPath)
        
        #Saving dict to *.json file
        with open(jsonFilePath, "w") as jsonFile:
            json.dump(daySummary, jsonFile, indent=2)


class InputTools:
    """
    Class where are stored methods used during user input.
        Required modules:
            curses
            datetime
    """

    #Class properties
    #Config for cls.OptionsMenuHandler()
    config = {"UpKey": "w",
              "DownKey": "s",
              "ChooseKey": "a"}

    #Class methods
    @classmethod
    def InputDate(cls, stdscr: curses.window, row: int, column: int) -> datetime.date:
        """
        Methods serves as input field for dates.
            Arguments:
                stdscr - curses.window object (available in curses module)
                row, column - posision of imput field.
            Returned values:
                datetime.date type boject that represent date typed by user or 'None' if this date does not exist.
        """
        #Creating list of digits in string type - required for input validation
        stringNumbers = [str(number) for number in range(10)]

        #Date user input
        stdscr.addstr(row, column, "YYYY-MM-DD")
        year = int(InputTools.InputString(stdscr, row, column, "YYYY", stringNumbers))
        month = int(InputTools.InputString(stdscr, row, column + 5, "MM", stringNumbers))
        day = int(InputTools.InputString(stdscr, row, column + 8, "DD", stringNumbers))
        
        #Date validation
        try:
            date = datetime.date(year, month, day)
        except ValueError:
            #If validation fails method will be called once more
            return cls.InputDate(stdscr, row, column)

        return date

    @classmethod
    def OptionsMenuHandler(cls, stdscr: curses.window, optionsStrings: list, row: int, column: int) -> int:
        """
        Method used for creating choose menu.
            Arguments:
                stdscr - curses.window object (availabel in curses module).
                optionsStrings - list conatining strings that be used for displaying content of choose menu.
                row, column - posision of imput field.
            Returned values:
                int represented index of option chosen bu user form optionsStrings
        """

        #Determinig max index available in optionsStrings
        maxOptionStingsIndex = len(optionsStrings) - 1

        #Generating style for each option. First option in optionsStrings will be highlighted at the beggining
        optionStyles = [curses.A_REVERSE]
        for _ in range(maxOptionStingsIndex):
            optionStyles.append(curses.A_NORMAL)       

        #Printing options
        currentRow = row
        for optionString, optionStyle in zip(optionsStrings, optionStyles):
            stdscr.addstr(currentRow, column, optionString, optionStyle)
            currentRow += 1

        #Implementation of navigation between options
        highlightedOption = 0
        while True:
            #Waiting for user input
            inputChar = stdscr.getkey()

            #Moving up
            if inputChar == cls.config["UpKey"]:
                highlightedOption -= 1
                if highlightedOption < 0:
                    highlightedOption = 0

            #Moving down
            if inputChar == cls.config["DownKey"]:
                highlightedOption += 1
                if highlightedOption > (maxOptionStingsIndex):
                    highlightedOption = (maxOptionStingsIndex)

            #If 'choose key' will be clicked index of chosen option from optionsStrings will be returned
            if inputChar == cls.config["ChooseKey"]:
                return highlightedOption

            #Updating highlight in options menu
            for index , _ in enumerate(optionStyles):
                if index == highlightedOption:
                    optionStyles[index] = curses.A_REVERSE
                else:
                    optionStyles[index] = curses.A_NORMAL
            
            #Prionting updated options menu
            currentRow = row
            for optionString, optionStyle in zip(optionsStrings, optionStyles):
                stdscr.addstr(currentRow, column, optionString, optionStyle)
                currentRow += 1

    #Static methods
    @staticmethod
    def InputTime(stdscr: curses.window, row: int, column: int) -> datetime.time:
        """
        Method used for creating input fields specyfic for time.
        Time that can be inputed is limited to hours and minutes.
        Input field in its inital form has foramt 'HH:MM'.
            Argumets:
                stdscr - curses.window object (available in curses module)
                row, column - posision of imput field.
            Returned values:
                datetime.time object corresponding to user input
        """

        #Creating list of digits in string type - required for input validation
        stringNumbers = [str(number) for number in range(10)]

        #User input implementation.
        hoursD1 = InputTools.InputString(stdscr, row, column, "H", stringNumbers[:3])    #First digit
            
        if hoursD1 == "2": #Second digit input pool is depending on first digit value
            hoursD2 = InputTools.InputString(stdscr, row, column + 1, "H", stringNumbers[:4])
        else:
            hoursD2 = InputTools.InputString(stdscr, row, column + 1, "H", stringNumbers)

        minutedD1 = InputTools.InputString(stdscr, row, column + 3, "M", stringNumbers[:6])  #First digit
        minutedD2 = InputTools.InputString(stdscr, row, column + 4, "M", stringNumbers)      #Second digit

        #Convering inputted chars to ISO format that is required for creating datetime.time typer object 
        isoFormat = f"{hoursD1}{hoursD2}:{minutedD1}{minutedD2}"

        return datetime.time.fromisoformat(isoFormat)

    @staticmethod
    def InputString(stdscr: curses.window, inputFieldRow: int, inputFieldColumn: int, inputFieldFilling: str, inputPool: list) -> str:
        """
        Function serves as input field.
            Arguments:
                stdscr - curses.window object (availabel in curses module)
                inputFieldRow, inputFieldColumn - posision of imput field
                inputFieldFilling - characters that will be used ass input field filling. Lenght of this argument is equal to number of expected characters to input.
                inputPool - list of characters that are allowed to use by user
            Return values:
                string inputted by user
        """
        #Creating list that represents current state of inptu field
        userInput = [char for char in inputFieldFilling]
        charsToInput = len(userInput)

        #Implementation of input field
        charsCouner = 0
        while charsCouner < charsToInput:
            inputChar = stdscr.getkey()
            if inputChar in inputPool:
                userInput[charsCouner] = inputChar
                charsCouner += 1
                stdscr.addstr(inputFieldRow, inputFieldColumn, f"{''.join(userInput)}")

        #Returning 'userInput' as string
        return "".join(userInput)


class Menus:
    """
    Class used for storing methods that are implementatnions of pragram menus.
        Required modules:
            curses
            datetime
        Required external classes:
            ArchiveMenager
            InputTools
    """

    #Class methods
    @classmethod
    def __CheckDayFromArchive(cls, stdscr: curses.window, companyName: str) -> None:
        """
        Implementation of menu where user can check one archivised day.
        Details from corresponding to inputted date will be displayed.
            Arguments:
                stdscr - curses.window object (availabel in curses module)
                companyName - name of company. Required for using ArchiveMenager.LoadFromJson() method.
        """
        #Errasing window content
        stdscr.erase()
        
        #Creating frame
        textpad.rectangle(stdscr, 1, 1, 17, 71)
        stdscr.addstr(1, 29, "WHArchiviser")

        #Date user input
        stdscr.addstr(3, 3, "Date:")
        date = InputTools.InputDate(stdscr, 3, 9)

        #Iporting date
        workPeriods = ArchiveMenager.LoadFromJson(companyName, date)


        #Printing all work periods
        currentRow = 4                  #Row cursor init
        totalSeconds = 0                #Variable used for calculating total duration

        if workPeriods is not None:     #Checking if requested day exists in archive
            for index, period in workPeriods.items():
                #Converting imported data
                tstart = datetime.time.fromisoformat(period["start time"])
                tend = datetime.time.fromisoformat(period["end time"])
                startDatetime = datetime.datetime(date.year, date.month, date.day, tstart.hour, tstart.minute)
                endDatetime = datetime.datetime(date.year, date.month, date.day, tend.hour, tend.minute)
                
                #Calculationg period duration
                duration = endDatetime - startDatetime
                hours, remainder = divmod(duration.seconds, 3600)
                minutes = divmod(remainder, 60)[0]

                #Printing row that represents current work period
                stdscr.addstr(currentRow, 3, f"Work period {index}: Start time: {period['start time']} End time: {period['end time']} Duration: {hours}h {minutes}min")

                #Updating variables
                totalSeconds += duration.seconds
                currentRow += 1

            #Converting seconds to more human readable format
            HumanReadable = cls.__SecondsToHoursAndMinutes(totalSeconds)

            #Printing 'Total duration' row
            stdscr.addstr(currentRow, 3, f"Total duration: {HumanReadable['hours']}h {HumanReadable['minutes']}min")
            currentRow += 1

            #Waiting for user interrupt
            stdscr.addstr(currentRow, 3, "Type any char to continue...")
            stdscr.getkey()
        else:
            #Waiting for user interrupt
            stdscr.addstr(4, 3, "That day does not exists in archive.")
            stdscr.addstr(5, 3, "Type any char to continue...")
            stdscr.getkey()

    @classmethod
    def __CreatePeriodSummary(cls, stdscr: curses.window, companyName: str) -> None:
        """
        Implementation of menu where user can generate summary of inputted period.
            Arguments:
                stdscr - curses.window object (availabel in curses module)
                companyName - name of company. Required for using ArchiveMenager.LoadFromJson() method.
        """
        #Errasing window content
        stdscr.erase()
        
        #Creating frame
        textpad.rectangle(stdscr, 1, 1, 26, 71)
        stdscr.addstr(1, 29, "WHArchiviser")
        
        #User input
        stdscr.addstr(3, 3, "Start date: ")
        startDate = InputTools.InputDate(stdscr, 3, 15)
        stdscr.addstr(3, 26, "End date: ")
        endDate = InputTools.InputDate(stdscr, 3, 36)

        #Initializing variables needed for creating period summary
        seconds = {"total":   0,
                   "Mon-Fri": 0,
                   "Sat-Sun": 0}

        days = {"in period": 0,
                "analyzed":  0}
        
        daysCounter = {"Monday": 0,
                       "Tuesday": 0,
                       "Wednesday": 0,
                       "Thursday": 0,
                       "Friday": 0,
                       "Saturday": 0,
                       "Sunday": 0
                       }

        workPeriodsTotal = 0

        #Initializing variables neaded for switching between days
        oneDayTimeDelta = datetime.timedelta(1)
        datePointer = startDate

        #Initializisng dict required to convert days names from int format to string format
        conversionDict = {0: "Monday",
                          1: "Tuesday",
                          2: "Wednesday",
                          3: "Thursday",
                          4: "Friday",
                          5: "Saturday",
                          6: "Sunday"
                       }

        #Alayzis of given period
        while True:
            #Importing data from archived *.json file
            workPeriods = ArchiveMenager.LoadFromJson(companyName, datePointer)

            #If *.json file correspodnig to current date pointer exists it will be analyzed.
            if workPeriods is not None:
                #Anzlyzing each work period individually
                for period in workPeriods.values():
                    #Converting imported data
                    tstart = datetime.time.fromisoformat(period["start time"])
                    tend = datetime.time.fromisoformat(period["end time"])
                    startDatetime = datetime.datetime(datePointer.year, datePointer.month, datePointer.day, tstart.hour, tstart.minute)
                    endDatetime = datetime.datetime(datePointer.year, datePointer.month, datePointer.day, tend.hour, tend.minute)
                
                    #Calculationg period duration
                    duration = endDatetime - startDatetime

                    #Updating variables
                    if datePointer.weekday() in range(5):
                        seconds["Mon-Fri"] += duration.seconds
                    else:
                        seconds["Sat-Sun"] += duration.seconds

                    seconds["total"] += duration.seconds

                    workPeriodsTotal += 1

                #Updating variables
                days["analyzed"] += 1 

                weekdayName = conversionDict[datePointer.weekday()]
                daysCounter[weekdayName] += 1

            #Updating variables
            days["in period"] += 1

            #If date pointer currenty points to the date given as end loop will be broken 
            if datePointer == endDate:
                break
            #If not date pointer is switched to the next day
            datePointer = datePointer + oneDayTimeDelta

        
        #Calculating average times
        try:
            averageWorkTimePerDay = int(seconds["total"]/days["analyzed"])
        except ZeroDivisionError:
            averageWorkTimePerDay = 0

        try:
            averageWorkTimePerWorkPeriod = int(seconds["total"]/workPeriodsTotal)
        except ZeroDivisionError:
            averageWorkTimePerWorkPeriod = 0

        #Converting collected time data to more human readable format - hours and minutes
        humanReadableTimes = {"total": cls.__SecondsToHoursAndMinutes(seconds["total"]),
                              "Mon-Fri": cls.__SecondsToHoursAndMinutes(seconds["Mon-Fri"]),
                              "Sat-Sun": cls.__SecondsToHoursAndMinutes(seconds["Sat-Sun"]),
                              "average per day": cls.__SecondsToHoursAndMinutes(averageWorkTimePerDay),
                              "average per period": cls.__SecondsToHoursAndMinutes(averageWorkTimePerWorkPeriod)
                              }

        #Printing output
        stdscr.addstr(5, 2, "===========================PERIOD SUMMARY============================")
        stdscr.addstr(6, 3, f"Total work duration: {humanReadableTimes['total']['hours']}h {humanReadableTimes['total']['minutes']}min")
        stdscr.addstr(7, 3, f"Total work duration from Monday to Friday: {humanReadableTimes['Mon-Fri']['hours']}h {humanReadableTimes['Mon-Fri']['minutes']}min")
        stdscr.addstr(8, 3, f"Total work duration over the weekends: {humanReadableTimes['Sat-Sun']['hours']}h {humanReadableTimes['Sat-Sun']['minutes']}min")
        stdscr.addstr(9, 3, f"Total work periods: {workPeriodsTotal}")
        stdscr.addstr(10, 3, f"Average work time per day: {humanReadableTimes['average per day']['hours']}h {humanReadableTimes['average per day']['minutes']}min")
        stdscr.addstr(11, 3, f"Average work time per work period: {humanReadableTimes['average per period']['hours']}h {humanReadableTimes['average per period']['minutes']}min")
        stdscr.addstr(12, 3, f"Days in given period: {days['in period']}")
        stdscr.addstr(13, 3, f"Days found in archive and analyzed: {days['analyzed']}")
        
        stdscr.addstr(15, 3, "Distribution of working days during the week:")

        currentRow = 16
        for day, quantity in daysCounter.items():
            try:
                stdscr.addstr(currentRow, 5, f"{day}: {quantity} ({quantity/days['analyzed'] * 100}%)")
            except ZeroDivisionError:
                stdscr.addstr(currentRow, 5, f"{day}: {quantity} (0%)")

            currentRow += 1

        stdscr.addstr(23, 2, "=====================================================================")
        stdscr.addstr(24, 3, "Type any char to continue...")
        stdscr.getkey()

    @classmethod
    def MainMenu(cls, stdscr: curses.window) -> None:
        """
        Method that implemets program main menu.
        Only that method is ment to be used externally.
            Arguments:
                stdscr - curses.window object (availabel in curses module)
        """

        #Errasing window content
        stdscr.erase()

        #Creating frame
        textpad.rectangle(stdscr, 1, 1, 10, 31)
        #Adding frame title
        stdscr.addstr(1, 10, "WHArchiviser")
        stdscr.addstr(3, 3, "Choose option:")

        #Creating options
        optionsStrings = ["- Add new day to archive",
                          "- Create period summary",
                          "- Check archived day",
                          "- Remove day from archive",
                          "- Quit"]
        
        #Creating options menu
        chosenOption = InputTools.OptionsMenuHandler(stdscr, optionsStrings, 4, 5)
        
        #Handling option chosen by user
        if chosenOption == 0:   #'Add new day to archive' option
            companyName = cls.__ChooseOrAddCompanyMenu(stdscr)
            cls.__AddDayMenu(stdscr, companyName)
            cls.MainMenu(stdscr)
        if chosenOption == 1:   #'Create period summary' option
            companyName = cls.__ChooseCompanyMenu(stdscr)
            if companyName is not None:
                cls.__CreatePeriodSummary(stdscr, companyName)
            cls.MainMenu(stdscr)
        if chosenOption == 2:   #'Check archived day' option
            companyName = cls.__ChooseCompanyMenu(stdscr)
            if companyName is not None:
                cls.__CheckDayFromArchive(stdscr, companyName)
            cls.MainMenu(stdscr)
        if chosenOption == 3:   #Remove day from archive
            companyName = cls.__ChooseCompanyMenu(stdscr)
            if companyName is not None:
                cls.__RemoveDayFromArchive(stdscr, companyName)
            cls.MainMenu(stdscr)

    #TBC
    @classmethod
    def __RemoveDayFromArchive(cls, stdscr: curses.window, companyName: str) -> None:
        """
        Implemetation of menu where user can remove *.sjon file corresponding to inputted date from archive.
            Arguments:
                stdscr - curses.window object (availabel in curses module)
                companyName - name of company. Required for using ArchiveMenager.RemoveJson() method.
        """
        
        #Errasing window content
        stdscr.erase()

        #Creating frame
        textpad.rectangle(stdscr, 1, 1, 26, 71)
        stdscr.addstr(1, 29, "WHArchiviser")

        stdscr.addstr(3, 3, "Date to remove: ")
        date = InputTools.InputDate(stdscr, 3, 19)

        #Checking if day exists in archive
        if ArchiveMenager.CheckIfJsonExists(companyName, date):
            stdscr.addstr(4, 3, "Do You really want to remove this day from archive?")
            answer = InputTools.OptionsMenuHandler(stdscr, ["No", "Yes"], 5, 3)
            if answer == 1:
                ArchiveMenager.RemoveJson(companyName, date)
        else:
            stdscr.addstr(5, 3, "This day does not exist in archive")
            stdscr.addstr(6, 3, "Type any char to contionue...")
            stdscr.getkey()

    #Static methods
    @staticmethod
    def __AddDayMenu(stdscr, companyName: str) -> None:
        """
        Method implements menu for adding new day to archive.
            Arguments:
                stdscr - curses.window object (availabel in curses module)
                companyName - name of company. Required for using ArchiveMenager.SaveToJson() method.
        """
        
        #Errasing window content
        stdscr.erase()

        #Creating frame
        textpad.rectangle(stdscr, 1, 1, 17, 52)
        stdscr.addstr(1, 18 , "WHARchiviser")

        #Date user input
        stdscr.addstr(3, 3, "Date:")
        date = InputTools.InputDate(stdscr, 3, 9)

        #Initialization of row cursor
        currentRow = 4

        #Working periods user input
        workPeriods = dict()    #format: {<number of period - int>: {"start time": <start time in format 'HH:MM' - str>, "end time": <end time in format 'HH:MM' - str>}}
        inputRequired = True
        while inputRequired:
            #User input for one work period
            stdscr.addstr(currentRow, 3, f"Work period {len(workPeriods)}: Start time: HH:MM End time: HH:MM")
            
            #Start time
            tstart = InputTools.InputTime(stdscr, currentRow, 30)

            #End time
            tend = InputTools.InputTime(stdscr, currentRow, 46)

            #Checking if start time is before end time
            startDatetime = datetime.datetime(date.year, date.month, date.day, tstart.hour, tstart.minute)
            endDatetime = datetime.datetime(date.year, date.month, date.day, tend.hour, tend.minute)
            
            if startDatetime >= endDatetime:
                continue

            #Adding work period to dict
            workPeriods[len(workPeriods)] = {"start time": tstart.isoformat("minutes"),
                                             "end time": tend.isoformat("minutes")}

            #Asking if user want to add another work period, max 10 periods allowed
            if len(workPeriods) < 10:
                #Creating options menu
                currentRow += 1
                stdscr.addstr(currentRow, 3, "Would You like to add another work time period?")
                currentRow += 1
                optionStrings = ["- No", "- Yes"]
                answer = InputTools.OptionsMenuHandler(stdscr, optionStrings, currentRow, 5)

                #Handling response for user request
                if answer == 0:
                    #Leaving work periods input loop
                    inputRequired = False
                if answer == 1:
                    #Arrasing content of options menu
                    stdscr.addstr(currentRow, 5, f"{'':4}")
                    currentRow += 1
                    stdscr.addstr(currentRow, 5, f"{'':5}")
                    currentRow -= 2
                    
            else:
                #Leaving work periods input loop
                inputRequired = False

        #Checking if input day already exists in archive - if yes, asking user for confirmation to overwrite existing day
        if ArchiveMenager.CheckIfJsonExists(companyName, date) is None:  
            #Saving day report to *.json file
            ArchiveMenager.SaveToJson(companyName, date, workPeriods)
        else:
            #Errasing window content
            stdscr.erase()

            #Creating frame
            textpad.rectangle(stdscr, 1, 1, 8, 39)
            stdscr.addstr(1, 13 , "WHARchiviser")

            #Creating options menu
            stdscr.addstr(3, 3, "That day already exists in archive.")
            stdscr.addstr(4, 3, "Do You want to overwrite it?")
            optionStrings = ["- No", "- Yes"]

            answer = InputTools.OptionsMenuHandler(stdscr, optionStrings, 5, 5)

            #Handling user sesponse
            if answer == 1:
                #Saving day report to *.json file
                ArchiveMenager.SaveToJson(companyName, date, workPeriods)


    @staticmethod
    def __ChooseCompanyMenu(stdscr: curses.window) -> str:
        """
        Menu from whitch user can choose company from archive.
            Arguments:
               stdscr - curses.window object (availabel in curses module) 
            Return values:
                Chosen company in string format.
        """
        #Errasing window content
        stdscr.erase()

        #Creating options menu
        options = ArchiveMenager.GetCompanies()

        #Calculating frame dimentions
        farmeWidth = 29
        for option in options:
            optionLen = len(option)
            if optionLen > farmeWidth:
                farmeWidth = optionLen + 2
        farmeWidth += 4

        #Creating frame
        textpad.rectangle(stdscr, 1, 1, len(options) + 5, farmeWidth)
        frameTitleColumn = int((farmeWidth - 12) / 2) + 1
        stdscr.addstr(1, frameTitleColumn, "WHArchiviser")

        #Checking if archive is empty
        if len(options) == 0:
            stdscr.addstr(3, 3, "Archive is empty.")
            stdscr.addstr(4, 3, "Type any char to contiune...")
            stdscr.getkey()

            return None

        #Printing options menu title row
        stdscr.addstr(3, 3, "Choose company from arvchive:")

        #Creating options menu
        answer = InputTools.OptionsMenuHandler(stdscr, [f"- {option}" for option in options], 4, 5)

        #Returning company name that already exists in archive
        return options[answer]

    @staticmethod
    def __ChooseOrAddCompanyMenu(stdscr: curses.window) -> str:
        """
        Extension of method '__ChooseCompanyMenu' where user additionally can type name of company that is not currently exist in archive.
            Arguments:
               stdscr - curses.window object (availabel in curses module) 
            Return values:
                Chosen company in string format.
        """
        #Errasing window content
        stdscr.erase()

        #Creating options menu
        options = ArchiveMenager.GetCompanies()
        options.append("Add new company to archive")

        #Calculating frame dimentions
        farmeWidth = 44
        for option in options:
            optionLen = len(option)
            if optionLen > farmeWidth:
                farmeWidth = optionLen + 2
        farmeWidth += 4

        #Creating frame
        textpad.rectangle(stdscr, 1, 1, len(options) + 5, farmeWidth)
        frameTitleColumn = int((farmeWidth - 12) / 2) + 1
        stdscr.addstr(1, frameTitleColumn, "WHArchiviser")

        #Printing options menu title row
        stdscr.addstr(3, 3, "Choose company from arvchive or add new one:")

        #Creating options menu
        answer = InputTools.OptionsMenuHandler(stdscr, [f"- {option}" for option in options], 4, 5)

        #Handling response for user request
        if answer == (len(options) - 1):    #Creating new company
            #Errasing window content
            stdscr.erase()

            #Creating frame
            textpad.rectangle(stdscr, 1, 1, 6, 38)
            stdscr.addstr(1, 3, "WHArchiviser")

            #User input
            stdscr.addstr(3, 3, "Type the name of the new company:")
            curses.echo()
            newCompanyName = stdscr.getstr(4, 3, 34)
            curses.noecho()

            #Returning new company name
            return newCompanyName.decode()
        else:
            #Returning company name that already exists in archive
            return options[answer]

    @staticmethod
    def __SecondsToHoursAndMinutes(seconds: int) -> dict:
        """
        Methods used for converting number of seconds to hours and minutes.
            Arguments:
                seconds - number of seconds that are ment to be converted
            Return value:
                Dict reporesenting relust of convertion in following format: {"hours": <number of hours: int>, "minutes": <number fo seconds: int>}
        """

        #Conversion
        hours, remainder = divmod(seconds, 3600)
        minutes = divmod(remainder, 60)[0]

        #REsults returned as dict
        return {"hours": hours, "minutes": minutes}
            
#Functions
def main(stdscr):
    """
    Main function of the program.
    """
    #Cathing of 'curses.error' exception that is raised when terminal window is too small to handle program propertly
    try:
        Menus.MainMenu(stdscr)
    except curses.error:
        raise curses.error("Terminal window is too small to handle this program propertly. Please maximise terminal window.")

#Main part of the program
if __name__ == "__main__":
    curses.wrapper(main)