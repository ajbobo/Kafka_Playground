# The -v parameter links the .aws folder on the local machine to the running image - Set it to your .aws directory
# This gives the image access to your AWS creds
# This command will automatically attach to the container
# To disconnect:
#   root@123:/# exit -- This will stop the container
#   Ctrl-P Ctrl-Q -- This will detach from the container without stopping it

docker run -v c:/users/artie.bobo/.aws:/root/.aws -it --name kafka_cli kafka-cli:1.0