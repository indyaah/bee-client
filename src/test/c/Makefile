# Warning - does not work yet!

COPTS = -D__cplusplus 

digtest: digtest.o
	ld digtest.o digcalc.o -o $@

digtest.o: digtest.c digcalc.o
	cc digtest.c $(COPTS) -o $@

digcalc.o: digcalc.c digcalc.h
	cc digcalc.c $(COPTS) -o $@


