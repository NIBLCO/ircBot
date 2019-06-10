# Intro
This is the ooinuza project for #nibl

# Setup
Install Mysql Docker Container

Compile and Run Ooinuza Docker Container

## Required Software
	Docker

## Database
#### Create mysql container
```
docker network create -d bridge my-bridge
docker pull mysql:8
docker run --network my-bridge --name niblmysql -e MYSQL_ROOT_PASSWORD={somerootpassword} -d mysql
docker exec -i niblmysql mysql -uroot -p{somerootpassword} < db/ooinuza.sql
```

#### Create mysql user
```
docker exec -it niblmysql bash
mysql -u root -p{somerootpassword}
CREATE USER 'ooinuza'@'172.18.0.%' IDENTIFIED BY 'niblempire';
GRANT ALL PRIVILEGES ON 'ooinuza'.* TO 'ooinuza'@'172.18.0.%';
FLUSH PRIVILEGES;
```
## Run App
The public IP is needed to enable dcc chat

Only one range of ports is allowed for dcc chat and must match entries in config/config.txt (todo fix this)

```
docker run -d \
--net='my-bridge' \
--name=ircbot \
--add-host public_dcc_ip:{publicIp} \
-p 46571-46572:46571-46572 \
-v /opt/ircBot/data/:'/data':'rw' \
-v /opt/ircBot/config/:'/config':'rw' \
ircbot
```