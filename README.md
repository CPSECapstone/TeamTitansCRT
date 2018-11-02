# Welcome to the homepage of MyCRT
MyCRT is a Capture & Replay tool for any AWS RDS instance. The tool was designed to attach to an existing AWS setup and monitor the load on a database. After capturing the requests and queries hitting the database, they are stored on an S3 bucket. That saved load can then be replayed onto other RDS instances with different configurations and comapred against the original. This provides a baseline to test all RDS setups against, and that comparison can be performed on all available CloudWatch metrics through our analysis tool.

## Features
- Capture the load on a database over a period of time
- Replay saved loads for use on other databases 
- Real-time metrics about the performance of an database
- Compare multiple databases against the same load for testing
- Select from every available CloudWatch metric to understand the how each RDS performs on the given load
- Visually anylize the comparison using our graphical Analysis tool

## Acknowlegements
This project was sponsored by Amazon through the Cal Poly Senior Capstone Project. Thanks again to Dr. Janzen for teaching the course and providing guidance throughout the year-long process.

**The team**:
- Devon Martin
- Amy Lewis
- Devin Nicholson
- Shiv Sulkar
- Robert Weber
- Kyle Ringler
- Michael McCaniff
