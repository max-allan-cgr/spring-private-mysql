```
docker build -t fips .
docker run -dp 8080:8080 fips
curl localhost:8080/getsecret
```

IF you are not on an EC2 instance with an instance role, you can add your AWS profile to the SpringController.java

Then run the container with:
```
docker run -d -p 8080:8080 --rm --name fips -v $HOME/.aws:/home/java/.aws -eAWS_PROFILE fips
```
(Assuming you set your profile in AWS_PROFILE already)

```
curl localhost:8080/getsecret
secretname=RDSdevdbsecret:(Secret may not render properly due to the formatting of the password):{"password":"<>{G!,z9evv2TP&{#gYSYuj6zD","engine":"mysql","port":"3306","dbInstanceIdentifier":"javacdkprivatemysqlstack-rds","host":"javacdkprivatemysqlstack-rds.ctpmhxcf90zn.us-west-2.rds.amazonaws.com","username":"RDSDEV_username"}%                                                              
```
(those DB and password values are not real! Sorry hackers!)
