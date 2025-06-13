# UselessProgram

A Java application that runs in the background and, at random intervals, simultaneously takes a screenshot of your screen and a photo from your computer's webcam. It then displays both images side by side in a window that appears above all other programs for a few seconds, following a "Snapchat-like" logic. The window disappears automatically, and the process repeats.

## Features
- Runs in the background
- Takes screenshots and webcam photos at random intervals
- Displays images side by side for a few seconds
- Window always appears on top and closes automatically
- Scales images to fit screen size
- Works on all major platforms including Apple Silicon Macs

## Requirements
- Java 17 or newer
- Maven 3.6 or newer
- Linux, Windows, or macOS (including Intel and Apple Silicon Macs)
- Webcam

## Installation

### Download
You can download the latest release from the [Releases](https://github.com/sikzone/UselessProgram/releases) page.

### Build from Source
1. Clone the repository:
```bash
git clone https://github.com/sikzone/UselessProgram.git
cd UselessProgram
```

2. Build with Maven:
```bash
mvn package
```
This will create an executable JAR with all dependencies in `target/useless-program-0.0.1-jar-with-dependencies.jar`

3. Run:
```bash
java -jar target/useless-program-0.0.1-jar-with-dependencies.jar
```

## Configuration
The following parameters can be adjusted in `Main.java`:
- `minDelay`: Minimum time between captures (default: 5 seconds)
- `maxDelay`: Maximum time between captures (default: 10 seconds)
- `displayTime`: How long images are displayed (default: 3 seconds)

## Technical Details
- Uses JavaCV for cross-platform webcam support
- Built with Java 17 and Maven
- Automatic image scaling to fit screen resolution
- Efficient resource management and memory usage

## License
See the LICENSE file for details.

## Version History
- 0.0.1: Initial release
  - Basic screenshot and webcam capture functionality
  - Random interval captures
  - Image scaling
  - Cross-platform support
