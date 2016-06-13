# CI Docker Demo

## Prerequisites (Mac)

You should have Docker Toolbox installed, see https://www.docker.com/toolbox

### Step 1 - Check Docker Machine version

Ensure that you are using version 0.3.0 or greater of `docker-machine`

```
# docker-machine version
docker-machine version 0.7.0, build
```

### Step 2 - Start Docker Machine

Start the machine, using the `--virtualbox-memory` option to increase it’s memory.
I use 6000 MB to accommodate all the docker images.

```
# docker-machine create -d virtualbox --virtualbox-memory "6000" default
Running pre-create checks...
```

### Step 3 - Set Docker Machine Connection

Configure shell environment to connect to your new Docker instance

```
# eval “$(docker-machine env default)”
```

## Getting started

To get all docker containers up and running use:

```
# git clone https://github.com/cloveryu/ci-docker.git
# cd ci-docker
# docker-compose up
```
