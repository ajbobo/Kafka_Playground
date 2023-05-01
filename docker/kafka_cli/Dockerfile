FROM azul/zulu-openjdk:11-latest

RUN apt update
# There are some packages are aren't in the base image to reduce its size; Install the ones we need
RUN apt install mandoc less wget curl unzip vim jq -y

# Get Kafka
RUN curl https://dlcdn.apache.org/kafka/3.4.0/kafka_2.13-3.4.0.tgz -o kafka_2.13-3.4.0.tgz
RUN tar -xzf kafka_2.13-3.4.0.tgz
ENV PATH="/kafka_2.13-3.4.0/bin:$PATH"

# Get AWSCli
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install

# Add a configuration to get Kafka to talk to AWS
COPY kafka.properties /kafka.properties
RUN wget https://github.com/aws/aws-msk-iam-auth/releases/download/v1.1.1/aws-msk-iam-auth-1.1.1-all.jar -O /kafka_2.13-3.4.0/libs/aws-msk-iam-auth-1.1.1-all.jar

# Add some files to make using the image easier
COPY set_aws_environment.sh /set_aws_environment.sh
COPY README /README