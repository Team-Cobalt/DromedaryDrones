# Dromedary Drones

![build & test](https://github.com/Team-Cobalt/DromedaryDrones/workflows/build%20&%20test/badge.svg?branch=master) ![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/Team-Cobalt/DromedaryDrones?include_prereleases)

This README covers the installation, startup, and use of team Cobalt's simulated drone delivery service.

**Software Used**<br>
\- Eclipse IDE for Java Developers - 2020-03<br>
\- JDK-13.0.2<br>
\- JavaFX 11.0.2<br>

# Table of Contents

- [Dromedary Drones](#dromedary-drones)
- [Table of Contents](#table-of-contents)
- [Installation](#installation)
  - [Eclipse](#eclipse)
    - [Cloning the Repository](#cloning-the-repository)
      - [Source Git Repository](#source-git-repository)
      - [Branch Selection](#branch-selection)
      - [Local Destination](#local-destination)
    - [Setting Up Java](#setting-up-java)
      - [Importing the JRE](#importing-the-jre)
      - [JRE Type](#jre-type)
      - [JRE Definition](#jre-definition)
      - [Applying Changes](#applying-changes)
    - [Setting Up JavaFX](#setting-up-javafx)
    - [Running the Program](#running-the-program)
- [Using the Program](#using-the-program)
  - [Running the Simulation](#running-the-simulation)
  - [Saving the Results](#saving-the-results)
  - [Saving the Settings](#saving-the-settings)

# Installation

## Eclipse

This guide walks through installing and running the Sprint1 branch of the project using [Eclipse IDE for Java Developers][eclipse-website] version 2020-03.

### Cloning the Repository

From your workspace, navigate to **Window > Show View > Other...**<br>
Within the new **Show View** window, select the **Git Repositories** option within the **Git** dropdown category and click **Open**.

![opening the version control view][eclipse-clone1]

From the new **Git Repositories** view at the bottom left, select the **Clone a Git repository** option.

![opening the clone window][eclipse-clone2]

#### Source Git Repository

Fill in the fields of opened **Clone Git Repository** menu with the following details and then select **Next**.

```
URI:              https://github.com/Team-Cobalt/DromedaryDrones.git
Host:             github.com
Respository path: /Team-Cobalt/DromedaryDrones.git

User:             your GitHub account username
Password:         your GitHub account password
```

![filling out the clone menu][eclipse-clone3]

#### Branch Selection

Make sure `sprint1` is selected and click **Next**.

![selecting the sprint1 branch][eclipse-clone4]

#### Local Destination

Specify the directory you wish to save the cloned project to. Make sure `sprint1` is selected for the **Initial branch** and that you have the __Import all existing Eclipse projects after clone finishes__ checkmarked.

And with that, click the **Finish** buton at the bottom to start the clone.

![finishing up the clone][eclipse-clone5]


### Setting Up Java

> This project uses `JDK-13.0.2` which can be obtained [here][java-13-download].

From your workspace, navigate to **Window > Preferences**.

![opening window preferences][eclipse-java1]

#### Importing the JRE

From the new **Preferences** window, navigate to **Java > Installed JREs**.

> If JDK-13 already appears in the list of installed JREs, you can skip to the [next section](#setting-up-javafx). 

Select the **Add...** button on the right.

![installed JREs][eclipse-java2]

#### JRE Type

Make sure `Standard VM` is selected and click **Next**.

![selecting jre type][eclipse-java3]

#### JRE Definition

Click the **Directory** button on the right and navigate to where your JDK 13 was installed. And with that, click the **Finish** button at the bottom right.

![filling in jre attributes][eclipse-java4]

#### Applying Changes

Back in the **Installed JREs** page, make sure your `jdk-13.x.x` JRE is selected and click **Apply and Close** at the bottom right.

![applying jre-13][eclipse-java5]

### Setting Up JavaFX

> This project uses `JavaFX LTS 11.0.2` which can be obtained [here][javafx-11-download].

Unfortunately, JavaFX is not included with JDK-13 and needs to be installed seperately.<br>
This README will not cover installing JavaFX. Please refer to [this amazing tutorial][javafx-11-install] where you will find instructions for installing and setting up JavaFX within the **JavaFX and Eclipse** category in the sidebar.

Pay close attention to the section pertaining to VM arguments!

### Running the Program

The main class is located at `DromedaryDrones > src > mainapp > MainClass.java`.
Right click on the class and select **Run As > 1 Java Application**.

![path to main class][eclipse-java6]

# Using the Program

![program home page][program-home]

## Running the Simulation
- It's very easy. Just click the **Start Simulation** button on the initial home screen.

## Saving the Results
- After running the simulation, you can export the results to a CSV file via the **Save Results** button at the bottom right.

![program results page][program-results]

## Saving the Settings
1. From the home screen, select **Settings**.
2. From the initial settings page, select **Save Changes**.

![program settings page][program-settings1]




[eclipse-website]: https://www.eclipse.org/eclipseide/

[eclipse-clone1]: images/eclipse-clone1.jpg
[eclipse-clone2]: images/eclipse-clone2.jpg
[eclipse-clone3]: images/eclipse-clone3.jpg
[eclipse-clone4]: images/eclipse-clone4.jpg
[eclipse-clone5]: images/eclipse-clone5.jpg

[eclipse-java1]: images/eclipse-java1.jpg
[eclipse-java2]: images/eclipse-java2.jpg
[eclipse-java3]: images/eclipse-java3.jpg
[eclipse-java4]: images/eclipse-java4.jpg
[eclipse-java5]: images/eclipse-java5.jpg
[eclipse-java6]: images/eclipse-java6.jpg

[program-home]: images/program-home.jpg
[program-results]: images/program-results.jpg
[program-settings1]: images/program-settings.jpg

[java-13-download]: https://www.oracle.com/java/technologies/javase-jdk13-downloads.html

[javafx-11-download]: https://gluonhq.com/products/javafx/
[javafx-11-install]: https://openjfx.io/openjfx-docs/#install-javafx
