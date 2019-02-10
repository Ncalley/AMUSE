# AMUSE framework (Advanced MUsic Explorer)

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
 
 ## Changelog :

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
java -jar target/Amuse-maven-0.3.1-SNAPSHOT.jar
``

To find further informations on Amuse's way of functionning, please read the **user_manual** located in the **docs** folder.