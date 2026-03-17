# Use an official C++ compiler image
FROM gcc:latest

# Install the Redis C++ library
RUN apt-get update && apt-get install -y libhiredis-dev

# Set up our working directory
WORKDIR /app

# Copy the source code in
COPY main.cpp .

# Compile it!
RUN g++ main.cpp -o agent -lhiredis

# Run the agent when the container starts
CMD ["./agent"]