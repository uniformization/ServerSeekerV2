[![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-%234169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JDA](https://img.shields.io/badge/JDA-%235865F2?style=for-the-badge&logo=discord&logoColor=white)](https://jda.wiki/)
[![Java](https://img.shields.io/badge/java-21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)

# ServerSeekerV2

ServerSeekerV2 is a better version of the original ServerSeeker. After being sold to a third party that did a huge blunder and got the entire project killed, I decided to take a crack and make my own Minecraft scanner.

Currently, the only IP address used by me to scan for servers is ``45.135.194.65`` if you are seeing join requests in your server from the same username but a different IP address, it is not me.

If you wish to request a server be removed from the public database and prevented from being scanned again, join my [Matrix Space](https://matrix.to/#/#projects:funtimes909.xyz) (**Much preferred**) or [Discord Server](https://discord.gg/wYTe2ZwD7g) and message ``@me:funtimes909.xyz`` directly.

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

## FAQ
- Q: What is this?
- A: ServerSeekerV2 is a faster version of the original ServerSeeker, it pings around 4 billion IPv4 addresses every few hours and attempts to join Minecraft servers on the ones that respond. This process is repeated over and over again.

- Q: How can I get my server removed?
- A: Join my [Matrix Space](https://matrix.to/#/#projects:funtimes909.xyz) (**Much preferred**) or [Discord Server](https://discord.gg/wYTe2ZwD7g) and ping ``@me:funtimes909.xyz`` or someone with moderator privileges asking for it to be removed.


- Q: How can I protect my server?
- A: Enable a whitelist for your server, a whitelist allows only specified players to join your server, run ``/whitelist on`` and then ``/whitelist add <player>`` for every player that will join your server. Additionally, setting "online-mode" to true in the ``server.properties`` file helps a lot


- Q: Why?
- A: As mentioned above, the previous owner of the original ServerSeeker, sold it to a third party, that got the discord bot and server terminated within a month of the sale (lmfao). At the time I was looking for a project to sink my endless amounts of free time into, so shortly after the sale, I started developing this :)

## Related projects
- [Discord Bot](https://git.funtimes909.xyz/ServerSeekerV2/ServerSeekerV2-Discord-Bot)
- [PyAPI](https://git.funtimes909.xyz/ServerSeekerV2/ServerSeekerV2-PyAPI)

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
- [EngurRuzgar](https://github.com/EngurRuzgar): Documentation and providing me with valid testing servers and for maintaining the [Python API](https://github.com/Funtimes909/ServerSeekerV2-PyAPI)
- [coolGi](https://coolgi.dev/): Code cleanup and general tips
