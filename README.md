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

- [x] Compile files before upload via scp 
- [x] Create parent directories as needed 
- [ ] Give upload permissions to destination directory (chmod -R 777 classes/)  
- [ ] Support multiple servers upload (comma separated list in configuration)
- [ ] Restart application server & notify when server is ready
- [ ] Support .xml files

![alt tag](https://raw.githubusercontent.com/startappdev/PatchWizard/master/resources/screenshot001.png)
