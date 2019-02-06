set AMUSEHOME=%~dp0
set MavenRepo=${user.home}\.m2\repository
set MiglayoutLoc=%MavenRepo%\miglayout\miglayout\3.7
set WekaLoc=%MavenRepo%\nz\ac\waikato\cms\weka\weka-stable\3.6.14
set Log4jLoc=%MavenRepo%\log4j\log4j\1.2.16
java -classpath lib\amuse-gui.jar;lib\amuse-frame.jar;lib\amuse-utils.jar;config\node\extractor\extractorNode.jar;config\node\processor\processorNode.jar;config\node\trainer\trainerNode.jar;config\node\classifier\classifierNode.jar;config\node\validator\validatorNode.jar;config\node\optimizer\optimizerNode.jar;%MigLayoutLoc%\miglayout-3.7-swing.jar;%WekaLoc%\weka-stable.jar;%Log4jLoc%\log4j-1.2.16.jar;lib\xpp3.jar;lib\xstream.jar;lib\rapidminer.jar;lib\tritonus_remaining-0.3.6.jar;lib\tritonus_share-0.3.6.jar;lib\jl1.0.jar;lib\jama.jar;lib\launcher.jar;lib\vldocking.jar;lib\xerces.jar;lib\mp3plugin.jar;lib\jAudio.jar;lib\jhall.jar amuse.scheduler.gui.controller.WizardController