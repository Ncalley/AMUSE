
# AMUSE framework (Advanced Music Explorer)

Copyright 2006-2017 by code authors

Created at TU Dortmund, Chair of Algorithm Engineering
(Contact: <http://ls11-www.cs.tu-dortmund.de>) 

AMUSE is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

AMUSE is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with AMUSE. If not, see <http://www.gnu.org/licenses/>.

## Please note:

* Along with the core AMUSE classes you have received some software
  packages and libraries not all of which are subject to the AMUSE
  license terms. You can find the license texts of these libraries
  in the 'licenses' directory of AMUSE.
  
* This version of AMUSE is licensed under the AGPL (see the
  corresponding license file). Please contact us if you are
  interested in a different license, e.g. an OEM license for
  integrating AMUSE into a proprietary software product
  not licensed under the AGPL.
 
 ## Requirements :

To run Amuse on Maven, ensure that you have Maven installed by running this command :
``
mvn -v
``
You need **Maven** with version 3.5.4 or later to run Amuse.
If you don't have Maven installed, you can download and install it here :
https://maven.apache.org/download.cgi

To access every functionnality of Amuse, you may want to install Matlab.
You can download Matlab here :
https://fr.mathworks.com/downloads/

If you want to use Amuse's classification **with Keras** you will need the following programs :

 - **Python 3.7** : https://www.python.org/downloads/release/python-370/
 - **TensorFlow** : https://www.tensorflow.org/install
 - **Keras** : https://keras.io/#installation

If you already have Python and are running on Windows 10, you can simply run the file install.bat located on amuse/Keras/ directory to automatically install Tensorflow and Keras with pip.

 ## Changelog :

What's new on Amuse v0.3.2-SNAPSHOT :

 - **Keras** is now live ! You can now train a model and classify files with amuse (This feature is still in it's alpha version and some bugs may still appear).
 - A new **Requirements** section have been added to the README.

What's new on Amuse v0.3.1-SNAPSHOT :

- Amuse now uses Maven ! The new way to run it is described on the **How to run** section.
- You are currently reading one of the changes of this new version : **The README** in the markdown format
- The dependencies versions are managed in a much cleaner way now, it's easier to change one if needed.
- Keras' dependencies have been added to the project.

## How to run

You need to run the following commands in order to make Amuse work :
Be sure to execute them in Amuse's root folder.
The first stage compiles the project.

``
mvn clean package -DskipTests
``

Once Amuse is compiled you can execute it, the following command will open Amuse's User Interface.

``
java -jar target/Amuse-maven-0.3.2-SNAPSHOT.jar
``

To find further informations on Amuse's way of functionning, please read the **user_manual** located in the **docs** folder.