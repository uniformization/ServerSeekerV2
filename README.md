# ServerSeekerV2

ServerSeekerV2 is a full rewrite of the original ServerSeeker, it takes a plaintext output file from [masscan](https://github.com/robertdavidgraham/masscan).  
Using that input it sequentially pings each IP address, on the port returned with a [Server List Ping](https://wiki.vg/Server_List_Ping) which returns the servers information, this information then gets stored in a PostgreSQL database.
Unlike the original ServerSeeker, V2 has some extra features:
- Faster
- Open Source
- Configurable
- Self Hostable (Host your own scanner and database!)
- Support for searching servers based on Forge mods

**Please also note!!**
This is just a backend, there's no frontend for easily searching for servers from the database yet, I will make a discord bot and a page on my website to do this in time, so don't worry

## Currently under heavy development!!
ServerSeekerV2 is **NOT** production ready! Please report any issues that you find, although i'm probably aware of most of them already, it would be good to track them.

Currently, lacking features are:
- Async and concurrency support (it will be unusably slow for large scans)
- General code cleanup and refactoring

## Getting Started
Currently, there are no prebuilt jars, you will have to build it yourself, thankfully this is easy, simply clone or download the repository locally and run `./gradlew build` the jar should be in the build/libs folder

You will need to make an account with [ipinfo](https://ipinfo.io) as well, put your account token in the config.json file

## Storing data in the database

To actually store information in a database you will need to set up PostgreSQL:  

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
