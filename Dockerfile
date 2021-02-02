FROM hub.scireum.com/scireum/sirius-runtime:25

USER root
ADD target/release-dir /home/sirius
RUN chown sirius:sirius -R /home/sirius

USER sirius

VOLUME /home/sirius/instance.conf

EXPOSE 9000
