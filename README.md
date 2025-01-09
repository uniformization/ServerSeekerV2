[![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/NeedCoolerShoes/SuperSimpleEmoji)
[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-%234169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JDA](https://img.shields.io/badge/JDA-%235865F2?style=for-the-badge&logo=discord&logoColor=white)](https://jda.wiki/)
[![Java](https://img.shields.io/badge/java-21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
![GitHub Last Commit](https://img.shields.io/github/last-commit/Funtimes909/ServerSeekerV2?style=for-the-badge&logo=github)
![GitHub Commit Activity](https://img.shields.io/github/commit-activity/w/Funtimes909/ServerSeekerV2?style=for-the-badge&logo=github)
![Code Size](https://img.shields.io/github/languages/code-size/Funtimes909/ServerSeekerV2?style=for-the-badge&logo=github)
![Lines of Code](https://img.shields.io/endpoint?style=for-the-badge&logo=github&url=https://ghloc.vercel.app/api/Funtimes909/ServerSeekerV2/badge?filter=.java$&label=lines%20of%20code&color=blue)

# ServerSeekerV2

ServerSeekerV2 is a full rewrite of the original ServerSeeker in Java, it reads output from [masscan](https://github.com/robertdavidgraham/masscan).  
Using that as input it asynchronously pings each IP address, on the port returned with a [Server List Ping](https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Server_List_Ping) which returns the servers information, this information then gets stored in a PostgreSQL database.

ServerSeekerV2 is **NOT** associated with the original ServerSeeker at all, the original is hosted by a third party.

Unlike the original ServerSeeker, V2 has some extra features:
- Basic whitelist checking
- Player Tracking
- Open Source
- Self Hostable (Host your own scanner and database!)

## Goals
Some longer term goals I would like to add:
- Bedrock support.
- Use of a Minecraft account pool for a faster and more accurate whitelist/cracked server detection.
- Subproject to automatically log in to unwhitelisted servers with accounts from the account pool and send a friendly message in chat warning of being unprotected

## Getting Started
Currently, there are no prebuilt jars, you will have to build it yourself, thankfully this is easy, simply clone or download the repository locally and run `./gradlew buildShadow` the jar should be in the build/libs folder

## Related projects
- [Discord Bot](https://github.com/Funtimes909/ServerSeekerV2-Discord-Bot)
- [PyAPI](https://github.com/Funtimes909/ServerSeekerV2-PyAPI)

## Storing data in the database
To store information in a database you will need to set up PostgreSQL:  

### Installation
#### Ubuntu
```sh
sudo apt-get install postgresql
```
#### Arch
```sh
sudo pacman -S postgresql
```


### Configuration
```sh
sudo -u postgres psql
```
After that you should get a terminal like this  
```
postgres=#
```  
Run the commands below to create a new user:  
```sql
ALTER USER postgres with encrypted password 'your_password';
```
Then put the new password in the `config.json` file.

## Special thanks
- [EngurRuzgar](https://github.com/EngurRuzgar): Documentation and providing me with valid testing servers, maintaining the [Python API](https://github.com/Funtimes909/ServerSeekerV2-PyAPI)
- [coolGi](https://coolgi.dev/): Code cleanup and general tips
