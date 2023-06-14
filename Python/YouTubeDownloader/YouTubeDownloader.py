from datetime import datetime
from pathlib import Path
from pytube import YouTube, Stream
import shlex
import subprocess


class YouTubeVideo:
    """
    Class meant to be used as downloader of high quality/resolution content present on YouTube.

    To work properly it requires pytube module (developed on version 15.0.0)
    along with ffmpeg (developed on version 4.3.6-0+deb11u1). Should be launched preferably in Linux-based systems.
    """

    def __init__(self, hyperlink: str):
        """
        Args:
            hyperlink: YouTube video hyperlink.
        """

        # Properties init
        self.video = YouTube(hyperlink)

    @staticmethod
    def __validate_output_file(file: Path, extension: str) -> None:
        """
        Validates file path, that will be used as output path for other utilities implemented in this class.
        If one of performed checks fail, according exception will be raised.

        Args:
            file: File path, that will be validated.
            extension: Desired file extension.
        """

        # Performing checks
        if not file.parent.exists(): raise FileNotFoundError(f"Parent directory of '{file}' does not exist.")
        if file.exists(): raise FileExistsError(f"'{file}' already exist.")
        if file.suffix != extension: raise TypeError(f"'{file}' has invalid extension - '{extension}' expected.")

    def save_video_as(self, webm_file: Path) -> None:
        """
        Downloads the best quality video stream (basing on resolution as a primary criteria and FPS as secondary)
        and saves it to given *.webm file.
        Video will be encoded using VP9 codec as it is native for YouTube DASH.

        Args:
            webm_file: Path to output *.webm path.
        """

        # Validation of provided output file
        YouTubeVideo.__validate_output_file(webm_file, ".webm")

        # Extracting only DASH video streams encoded using VP9 codec
        vp9_streams = list(self.video.streams.filter(mime_type="video/webm", video_codec="vp9", adaptive=True))

        # Searching for highest quality stream basing on resolution as a primary criteria and FPS as secondary
        def sort_criteria(stream: Stream) -> tuple[int, int]:
            # Stream resolution in int format ('resolution' property stores str values like "144p", "480p" etc.)
            resolution: int = int(stream.resolution[:-1])

            # Stream FPS added as secondary criteria
            return resolution, stream.fps

        # First stream in sorted stream list should be the one with the best quality
        stream_to_save, *_ = sorted(vp9_streams, key=sort_criteria)

        # Downloading the stream
        stream_to_save.download(output_path=str(webm_file.parent), filename=webm_file.name)

    def save_audio_as(self, opus_file: Path) -> None:
        """
        Downloads the best quality audio stream (basing on bit rate) and saves it to given *.opus file.
        Audio will be encoded using Opus codec as it is native for YouTube DASH.

        Args:
            opus_file: Path to output *.opus path.
        """

        # Validation of provided output file
        YouTubeVideo.__validate_output_file(opus_file, ".opus")

        # Extracting only DASH audio streams encoded using Opus codec
        opus_streams = list(self.video.streams.filter(mime_type="audio/webm", audio_codec="opus", adaptive=True))

        # Searching for the best quality stream basing on bit rate
        stream_to_save, *_ = sorted(opus_streams, key=lambda stream: stream.bitrate, reverse=True)

        # Downloading the stream
        stream_to_save.download(output_path=str(opus_file.parent), filename=opus_file.name)

    def save_as(self, mp4_file: Path) -> None:
        """
        Downloads the best quality video and audio stream and merge it together using ffmpeg into one *.mp4 file.
        Video in output file will be encoded using H.264/AVC coded and audio will be encoded using AAC - those
        codecs were picked as most media players supports them combined with MP4 container.

        As YouTube utilise Dynamic Adaptive Streaming over HTTP (DASH), streams containing the best available
        audio and video in a single file (referred in pytube documentation  as “progressive" streams) are not available.
        The only way to download the best available quality content is to download video and audio separately
        and then post-process them with software like ffmpeg (suggestion available in pytube documentation).

        Args:
            mp4_file: Path to output *.mp4 path.
        """

        # Validation of provided output file
        YouTubeVideo.__validate_output_file(mp4_file, ".mp4")

        # Preparing temporary directory
        tmp_dir = Path(__file__).parent / f"tmp_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
        tmp_dir.mkdir()

        video_file = tmp_dir / f"tmp_video_only.webm"
        audio_file = tmp_dir / f"tmp_audio_only.opus"

        try:
            # Downloading video and audio separately
            self.save_video_as(video_file)
            self.save_audio_as(audio_file)

            # Preparing a ffmpeg command, that will merge downloaded audio and video
            command = f"ffmpeg -i {video_file} -i '{audio_file}' -map 0:v -map 1:a -c:v libx264 -c:a aac {mp4_file}"
            command = shlex.split(command)

            # Executing prepared command
            subprocess.run(command, check=True)

        finally:
            # Removing temporary elements from filesystem
            video_file.unlink(missing_ok=True)
            audio_file.unlink(missing_ok=True)
            tmp_dir.rmdir()
