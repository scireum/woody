FROM scireum/sirius-runtime:7

USER root
ADD target/release-dir /home/sirius
RUN chown sirius:sirius -R /home/sirius

USER sirius

VOLUME /home/sirius/instance.conf

EXPOSE 9000
