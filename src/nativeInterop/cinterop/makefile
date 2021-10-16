all: stb parson

BIN := bin

stb: ${BIN}/stb.o
	ar rcs ${BIN}/stb.a ${BIN}/stb.o

parson: ${BIN}/parson.o
	ar rcs ${BIN}/parson.a ${BIN}/parson.o

${BIN}/stb.o: stb.c
	gcc -c stb.c -o ${BIN}/stb.o

${BIN}/parson.o: parson.c
	gcc -c parson.c -o ${BIN}/parson.o
