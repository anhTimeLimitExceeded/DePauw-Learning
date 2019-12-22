# DePauw Learning

Repo to hold all of the ITAP 2019-2020 code.

## Getting Started
### Prerequisites

To install, you need two things:

[Java JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk12-downloads-5295953.html)

[NodeJS + NPM](https://www.taniarascia.com/how-to-install-and-use-node-js-and-npm-mac-and-windows/)

### Installing

TBD

## Development

Always begin with pulling the latest version from Git.

```
git pull
```

### Start development servers

> Assuming you are at the root directory of the project

To start the Frontend server:

```
cd client && npm run serve
```

To start the Backend server:

```
cd server
```

and then

```
mvn clean spring-boot:run
```

everytime you change a file (or you can set up a continous build system!).

The Frontend will be accessible at [http://localhost:3000](http://localhost:3000)

### Start new branch

To start a new branch:

```
git checkout -b [name_of_the_new_branch]
```

Then do all the commits you need on that branch.

To push the branch to GitHub:

```
git push origin [name_of_your_new_branch]
```

To merge the current branch to "master":

```
git merge
```

Commiting to "master" is disabled, that way we get used to code reviews.
