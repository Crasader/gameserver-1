--- How to use those script ---

1. protob_sed.sh is used to format the original Huaqingfeiyang's proto files into Java compatible format.
	It will insert proper package and java class names. So use it first, as follows:
	> ./protob_sed.sh

2. genProtoBufJava.sh is used to compile the .proto files by Google protoc compiler and generate java source codes.
	> ./genProtoBufJava.sh

3. genidtomessage.sh is used to generate utitily classes according to the protocol buffer files.
	> ./genIdMessageClass.sh

4. genIdMessageLua.sh is used to generate id to message mapping file for protocol buffer and copy them to clients
	> ./genIdMessageLua.sh

5. genhandlerandtests.sh is used to generate netty handler and client side test cases.
	> ./genHandlerAndTest.sh


If you add new protocols to protoc/extend dir, please execute the following script
	> ./ext1protoc.sh

Note the generate handler will be saved to gensrc/java. If you are content with it, copy it to src/java dir.

