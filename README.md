```
docker build -t fips .
docker run -dp 8080:8080 fips
curl localhost:8080/getsecret
```

IF you are not on an EC2 instance with an instance role, you can add your AWS profile to the SpringController.java

The intention was to provide a new default SSL context that AWS SDK would use.

Is that JCAJCE or JSSE?? I don't know, but it seemed nearly working with JSSE and then I got an error about secure random.
