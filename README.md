# PatchWizard
IntelliJ plugin for AdPlatform to ease patch upload 

This plugin compiles & upload **all** classes from the selected changelist

- Configuration can be found at File->Settings->Tools

- To execute run Code->Deploy to server

- While debugging the plugin, logs are written to file idea.log, to view logs execute:

```sh
less +F /home/.IntelliJIdea2016.1/system/plugins-sandbox/system/log/idea.log
```

### TODO List

- [ ] Add dependencies to manifest <a href="http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html">more info</a> 
- [x] Compile files before upload via scp 
- [x] Create parent directories as needed 
- [ ] Give upload permissions to destination directory (chmod -R 777 classes/), currently showing informative notification error  
- [ ] Support multiple servers upload (comma separated list in configuration)
- [ ] Restart application server & notify when server is ready
- [ ] Support .xml files upload

![alt tag](https://raw.githubusercontent.com/startappdev/PatchWizard/master/resources/screenshot001.png)
