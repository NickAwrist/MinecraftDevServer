## Minecraft Dev Server

An IntelliJ IDEA plugin designed to streamline the workflow for PaperMC plugin developers. It allows you to download, configure, and run Minecraft servers directly within the IDE, eliminating the need to manage external server directories and console windows manually.


### Features

1. Automatic setup:  
Fetches available PaperMC versions and builds (including pre-releases) via the Paper API and handles the download/installation process automatically.

2. Integrated Server Management:

    - Start, stop, and restart servers from the Servers tool window.

    - Embedded Console: View server logs and execute commands directly inside IntelliJ.

    - Quick Access: One-click buttons to open the local Server Folder or Plugins Folder in your system's file explorer.

3. Quick Deployment:  
Right-click any JAR file in your Project View and select Move to Dev Server to instantly transfer your compiled plugin to a running server instance.

### Usage
1. Creating a Server

   - Open the Servers tool window (located on the right sidebar by default).

   - Click Create New Server.

   - Enter a name, select your desired PaperMC version, and choose a specific build number.

   - The plugin will download the JAR and generate the necessary eula.txt and server.properties.

2. Running a Server

   - Click the server name in the list to view its details.

   - Click Start Server to boot up the instance.

   - Use the console pane at the bottom of the view to interact with the server.

3. Deploying Your Plugin

   - Build your project artifact.

   - In the Project View, right-click your generated .jar file.

   - Select Move to Dev Server -> [Your Server Name].

   - The file will be moved to the selected server's plugins directory.
