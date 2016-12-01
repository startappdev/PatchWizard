# PatchWizard
IntelliJ plugin for AdPlatform to ease patch upload 

This plugin compiles & upload classes from selected changelist.

- Configuration can be found at File->Settings->Tools

- To execute run Code->Deploy to server

- While debugging the plugin, logs are written to file idea.log, to view logs execute:

```sh
less +F /home/.IntelliJIdea2016.1/system/plugins-sandbox/system/log/idea.log
```

### TODO List

- [x] Compile files before upload via scp 
- [ ] Support multiple servers upload (comma seprated list in configuration)
- [ ] Restart application server & notify when server is ready
- [ ] Support .xml files

![alt tag](https://raw.githubusercontent.com/startappdev/PatchWizard/master/resources/screenshot001.png)
