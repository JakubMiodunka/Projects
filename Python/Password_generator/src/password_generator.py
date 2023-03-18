import random
import string
import pathlib


######################################
#       Author: Jakub Miodunka       #
#            Version: 1              #
#       Last update: 26.11.2022      #
#   Used version of Python: 3.8.10   #
######################################


word_base_path = pathlib.Path("../config/popular.txt")


class WordPool:
    def __init__(self, path: pathlib.Path) -> None:
        # Arguemnts validaiotn
        assert path.exists(), f"Given source file for {self.__class__.__name__} instance does not exist"
        assert path.is_file(), f"Given source file for {self.__class__.__name__} instance is not a file"

        # Properties init
        self.path = path
        self.all_words = 0      # Quantity of all words stored in file
        self.valid_words = 0    # Quantity of words stored in file that are concidered as valid (lenght greater than 2 chars)

        # Content validaiton od given file
        with open(self.path) as pool:
            for line in pool:
                self.all_words += 1
                if len(line.strip()) >= 2:
                    self.valid_words += 1

    def pick_a_word(self) -> str:
        # Picking one random line (word) from source file 
        picked_line = random.randint(0, self.all_words - 1)

        # Opening a source file
        with open(self.path) as pool:
            # Searching for picked line
            for line, word in enumerate(pool):
                if line == picked_line:
                    # Returning word stored in picked line
                    return word.strip()


class PasswordGenerator:
    # Class properties init
    special_chars = [char for char in string.punctuation if char != "~"]    # '~' excluded from special chars pool

    letter_to_digit = {"o": "0",
                       "s": "2",
                       "e": "3",
                       "h": "4",
                       "b": "6",
                       "e": "8"}
    
    letter_to_special = {"a": "@",
                         "s": "$",
                         "i": "!"}

    @classmethod
    def generate_password(cls, seed: str) -> str:
        result = seed

        # Adding special characters
        try:
            # Trying to convert letters from given seed to special characters according to cls.letter_to_special
            result = cls._convert_to_special(result)
        except AssertionError:
            # If it's not possible special characters will be added as a 1-3 char long prefix or suffix (chosen randomly)
            result = cls._add_special(result, random.choice([False, True]))

        # Adding digits
        try:
            # Trying to convert letters from given seed to digits according to cls.letter_to_digit
            result = cls._convert_to_digits(result)
        except AssertionError:
            # If it's not possible digits will be added as a 1-3 char long prefix or suffix (chosen randomly)
            result = cls._add_digits(result, random.choice([False, True]))

        return result

    @classmethod
    def _convert_to_digits(cls, seed: str) -> str:
        # Argument validation
        assert [char for char in seed if char.lower() in cls.letter_to_digit], "Adding digits not possible"

        while True:
            # Performing char conversion according to cls.letter_to_digit - every char that can be converted has 50% chanse for being changed
            result = [cls.letter_to_digit.get(char.lower(), char) if random.randint(0, 1) == 0 else char for char in seed]

            # Checking if there is some digits in result
            if [char for char in result if char in cls.letter_to_digit.values()]:
                # Returning result as str
                return "".join(result)

    @classmethod
    def _convert_to_special (cls, seed: str) -> str:
        # Argument validation
        assert [char for char in seed if char.lower() in cls.letter_to_special], "Adding special character not possible"

        while True:
            # Performing char conversion according to cls.letter_to_special - every char that can be converted has 50% chanse for being changed
            result = [cls.letter_to_special.get(char.lower(), char) if random.randint(0, 1) == 0 else char for char in seed]

            # Checking if there is some special chars in result
            if [char for char in result if char in cls.letter_to_special.values()]:
                # Returning result as str
                return "".join(result) 

    @staticmethod
    def _randomize_letter_casing(seed: str) -> str:
        return "".join([char.upper() if random.randint(0, 1) == 0 and char in string.ascii_lowercase else char for char in seed])

    @classmethod
    def _add_digits(cls, seed: str, prefix: bool) -> str:
        if prefix:  # Adding digits as prefix
            return seed + "".join([random.choice(string.digits) for _ in range(random.randint(1, 3))])
        else:       # Adding digits as suffix
            return "".join([random.choice(string.digits) for _ in range(random.randint(1, 3))]) + seed

    @classmethod
    def _add_special(cls, seed: str, prefix: bool):
        if prefix:  # Adding special char as prefix
            return seed + "".join([random.choice(cls.special_chars) for _ in range(random.randint(1, 2))])
        else:       # Adding special char as suffix
            return "".join([random.choice(cls.special_chars) for _ in range(random.randint(1, 2))]) + seed


# Main part of the script
if __name__ == "__main__":
    # Initialising word pool
    word_pool = WordPool(word_base_path)

    print("Word pool file scanned")
    print(f"Rate: {round(word_pool.valid_words/word_pool.all_words * 100)}% ({word_pool.valid_words}/{word_pool.all_words})")

    password_seed = word_pool.pick_a_word()
    password = PasswordGenerator.generate_password(password_seed)

    while len(password) < 8:
        password_seed = word_pool.pick_a_word()
        password = PasswordGenerator.generate_password(password_seed)

    print(f"Seed: {password_seed}")
    print(f"Password: {password}")

