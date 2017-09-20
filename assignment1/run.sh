ant -f build_assign1.xml
export DATA_PATH="/Users/liangkeg/Documents/courses/11711/projects/project1/assign1_data"
java -cp assign1.jar:assign1-submit.jar -server -mx2000m \
edu.berkeley.nlp.assignments.assign1.LanguageModelTester \
-path $DATA_PATH -lmType UNIGRAM
