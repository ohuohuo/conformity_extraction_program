# Conformity Extraction Program

This program aims to find the Ground Truth (this will be used later in this entire project) and to test the Homophily assumption. 

The experimental input data is `Updated.txt`; it contains twitter data and their topic is "LeBron".
It also contains sentiment value of each tweet the user post.

`lebronjson_final.json` is the processed data which contains a lot of properties the `updated.txt` doesn't have.
These properties include the Ground Truth, sentiment bucket, majority neighbor opinion..etc

`lebronjson_homophily.json` is the result of homophily analysis from `lebronjson_final.json`.

classes in `GTandHomo.java`
-------------------------
There're 4 classes, they are:

`GTandHomo.class`: This class is the main class to hold everything inside including IO. Where main function lays.

`AddValue.class`: This class aims to add properties to raw twitter data so that following process can be done.

`GroundTruth.class`: This class aims to find Ground Truth and prepare the output data.

`Homo.class`: This class aims to test the Homophily Assumption (assumption that if two users are close in the social network, then their innate opinions are likely to be also close.) and prepare the output data.

