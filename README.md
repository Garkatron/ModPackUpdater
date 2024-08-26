# ModPackUpdater Guide

## Running the JAR File

### Steps for the User

1. **Close the "ModpackConfig" Window**:  
   - Ensure you have completed the initial configuration.

2. **Complete the App Configuration**:  
   - Provide the necessary details in the application configuration window (`app.conf`).

3. **Reopen the Application**:  
   - After closing the configuration window, reopen the application.
   - If the configuration was done correctly, the application will automatically download the mods and close upon completion.

   **Note**:  
   - If you've already downloaded the mod configuration (`modpack.conf`) previously, you'll only need to set up the download location for the mods.
   - If the application configuration (`app.conf`) was already downloaded, ensure that the `jarDownloadPath=path/to/your/folder` points to your desired folder.

4. **Update Notification**:
   - When you learn that your modpack has been updated, rerun the JAR file to download the latest mods.

### Creating a Modpack

1. **Configure the Modpack**:
   - Upon opening the window, you will be prompted to specify the number of mods and provide their direct download links from GitHub.

   **Example Link**:
https://github.com/Turnip-Labs/bta-halplibe/releases/download/4.1.3/halplibe-4.1.3.jar

**Important**:
- The link must point directly to the `.jar` file.
- Avoid including version numbers in the mod names so that files will overwrite old ones during updates.

2. **Upload the Modpack to GitHub**:
- Create a repository named `Modpacks` in your GitHub account.
- Inside this repository, create a folder with the name of your modpack.
- Upload a new release with the following format: `modpackname=version`, where the version should only consist of numbers.
