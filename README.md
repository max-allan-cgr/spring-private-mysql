```
docker build -t fips .
docker run -dp 8080:8080 fips
curl localhost:8080/getsecret
```

IF you are not on an EC2 instance with an instance role, you can add your AWS profile to the SpringController.java


