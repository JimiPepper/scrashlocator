SCRASHLOCATOR
===============

Romain Philippon
Maxime Douylliez
Quentin Bailleul

Lancer le projet
--------------------
ScrashLocator est un projet Scala construit avec SBT. À l'aide de votre terminal placez-vous dans le dossier du projet.
Pour lancer chacune des parties, veuillez entrer ces commandes
* Lancer le bucketing : sbt "run-main idl.bdp.scrashlocator.bucketing.Bucketing /chemin/absolu/bug/json/eclipse"
* Lancer une analyse Spoon : sbt -mem 2048 "run-main idl.bdp.scrashlocator.spoon.MethodAnalyzer /chemin/absolu/source/eclipse"
* Lancer le calcul du score de suspicion : sb t "run-main idl.bdp.scrashlocator.processing.Main --JsonReportFolderPath-- --JSONMethodLength-- --BucketId--"

Générer la documentation
--------------------------------
Pour générer la documentation, entrez la commande : sbt doc

Récupérer les sources
---------------------------
Cloner le dépôt Github suivant via la commande : git clone https://github.com/JimiPepper/scrashlocator.git

