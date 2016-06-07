JAVA_SRCS=	Pager.java

CLASSES=	$(JAVA_SRCS:.java=.class)

JAVAC=		javac
JFLAGS=		-g

RM=		rm -f

all:		$(CLASSES)

$(CLASSES):	$(JAVA_SRCS)
		$(JAVAC) $(JFLAGS) $^

clean:
		$(RM) $(CLASSES)
		$(RM) *.class

.PHONY:		clean
