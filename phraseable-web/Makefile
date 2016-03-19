.PHONY: conf
conf:
	@rm -f conf/application.conf
	@cp _setup/skel/conf/application.conf conf/.
	@rm -f conf/logback.xml
	@cp _setup/skel/conf/logback.xml conf/.
	@sbt playUpdateSecret

.PHONY: clean
clean:
	@sbt clean
	@rm -f conf/application.conf
	@rm -f conf/logback.xml
