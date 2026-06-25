# IoT PoC

The goal is to write a PoC demonstrating a working architecture of your IoT startup.

The typical architecture will have 5 components (probably 5 `Main`, and 5 `build.sbt`):

1. A program simulating the IoT/drone and sending drone-like data to your solution (see subject for details on a message). This program should not be distributed (no Spark). Your system will store messages in a distributed stream, making them available to components 2 and 4. (This part should not be done with Spark.)
2. Select alert messages from the stream.
3. Handle alert messages.
4. Store messages formatted as drone messages in a distributed storage — very likely a data lake (S3) with a bronze/silver/gold architecture.
5. Analyse stored data with a distributed processing component (like Spark). As a proof of your system's capacity to analyse the stored data, answer 4 questions of your choice (e.g., are there more alerts during the week or during the weekend?).

All components must be able to run independently, they must be scalable, and they must be used in a scalable way.

For component 4 you may use Kafka Connect or its equivalent (e.g., on AWS, Kinesis Firehose).

## Coding instructions

Any code must be written in functional Scala (compiling to JVM or JavaScript doesn't matter).

Unless I accept it as an exception, the keywords `for`, `while`, `return`, `var`, `throw`, `null`, mutable collections, `*Buffer`, `StringBuilder`, `try`, and `.sql` are forbidden, as is importing anything mutable. The method `.get` is also forbidden.

`foreach` as a collection (or RDD) method is accepted.

One exception for now: if you want to display a number of received/stored messages or alerts, you may use the keyword `var`.

Some students may choose, as a personal part, to code the five components in another functional language (F#, Haskell, etc.).

If you want to use Node.js for one of the five components, you may use it through Scala.js (example: <https://github.com/scalajs-io/nodejs>)

Spark code can either use DataFrames or RDDs, but don't mix them in the same pipeline.

## Submission (2 points)

For the project, you should use a Git repo (GitHub). Work from different members of the group should be visible in different commits.

For submission, you should email me at **adrien.broussolle+prof@gmail.com** with your Git repo and the last commit hash.

Your repository must be private and you should grant me access.

Late submission emails are accepted, minus 2 points per late day.

## Personal part

Once those 5 parts are done, you can work on the personal part.

The personal part is quite open: the goal is for every group to work on something they are curious about or that they find interesting for their CV. It can be done in the language of your choice, unless you are using Spark.

If you hesitate, I recommend option 0 or 1.

Here are some suggestions:

0. Use DuckDB or Polars for gold and stats.
1. Use dbt for gold and stats.
2. Run components (stream, compute, storage) scaled across several local computers each.
3. Project deployed on the cloud (Azure, AWS, GCP, etc.) using IaC like Terraform.
4. Use Docker and Docker Compose for the 5 components of the project (and Kubernetes/Mesos as a resource manager for Spark, if Spark is used).
5. Use a dashboard / data viz or custom website / dashboard to present the result of the Spark analysis (within an end-to-end pipeline).
6. Use an ML model and add information in the message to achieve predictive maintenance of the drone.
7. Whole project code in Haskell / F# / etc.
8. A mobile app to handle alerts.
9. Any idea you find relevant for the project, if I validate it.

## FAQ

- Except when doing the cloud option, everything can be written and deployed locally on your own computer (not distributed).
- What matters is to create a working and scalable PoC to demonstrate the architecture. Analysis pertinence doesn't matter.
- Scoring of the key metric is already handled in the drone. You don't have to write any code to adjust it, apart from your drone simulator.
- Most students should focus on the basic components. A group can achieve a good mark (up to 16/20) without the personal part. The personal part is for the curious groups who want to do more.
- If you want the cloud option to be 100% correct, all the components apart from the drone simulator should be running on the cloud.
- The presentation should be done with slides for the context and the architecture, and a small demo. Time given for the presentation + demo is 10–12 minutes (without questions).
