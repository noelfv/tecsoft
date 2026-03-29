FROM artifactory.globaldevtools.bbva.com:443/gl-cbs-swp2-docker-local/base-img/base-rh-openjdk17:v3

USER root

# Copiar los archivos
COPY --chown=swp2:swp2 *.jar /opt/swp2_gateway/
COPY --chown=swp2:swp2 entrypoint.sh /opt/swp2_gateway/
COPY --chown=swp2:swp2 proto/ /opt/swp2_gateway/proto/

# Dar permisos de ejecución al script de entrada
RUN chmod +x /opt/swp2_gateway/entrypoint.sh && \
    chmod +x /opt/swp2_gateway/*.jar

USER swp2

ENTRYPOINT ["/opt/swp2_gateway/entrypoint.sh"]