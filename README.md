# H2O-HW
QA Task for H2O

Data Set: https://archive.ics.uci.edu/ml/datasets/Reuters-21578+Text+Categorization+Collection

Create a REST api that allows you to explore the data set above. You can use any one of the .sgm files in the data set, you may import the data into a data store if you want to. You are expected to use Java/Python/Ruby/JavaScript and REST and any other technology of your choice
Expected APIs:
1. API to list content 
2. API to search content
3. API get a specific content by id/any identifier
Focus on the quality of the delivered solution, software delivery culture and good practices.
Make sure the solution could be deployed/executed without further assistance from you.
 
Share code git repo.

Implementation notes:
The original data set contains SGM files which look like XML because they contain XML tags and DTD file. Because of the SGM files are not valid XML, I used the similar dataset located at https://github.com/haseebr/irengine/tree/master/reuters21578-xml. These data are XML valid.
However, if you need to parse the original data set (SGM files), I can create a decorator which convert the SGM file into the valid XML file.

