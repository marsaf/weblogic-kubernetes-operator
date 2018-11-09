// Copyright 2017, 2018, Oracle Corporation and/or its affiliates.  All rights reserved.
// Licensed under the Universal Permissive License v 1.0 as shown at
// http://oss.oracle.com/licenses/upl.

package oracle.kubernetes.weblogic.domain.v2;

import static oracle.kubernetes.weblogic.domain.v2.ConfigurationConstants.START_IF_NEEDED;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.models.V1LocalObjectReference;
import io.kubernetes.client.models.V1SecretReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import oracle.kubernetes.json.Description;
import oracle.kubernetes.operator.VersionConstants;
import oracle.kubernetes.weblogic.domain.EffectiveConfigurationFactory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** DomainSpec is a description of a domain. */
@SuppressWarnings("NullableProblems")
public class DomainSpec extends BaseConfiguration {

  /** The pattern for computing the default persistent volume claim name. */
  private static final String PVC_NAME_PATTERN = "%s-weblogic-domain-pvc";

  /** Domain unique identifier. Must be unique across the Kubernetes cluster. (Required) */
  @SerializedName("domainUID")
  @Expose
  @NotNull
  private String domainUID;

  /** Domain name (Required) */
  @SerializedName("domainName")
  @Expose
  @NotNull
  private String domainName;

  /**
   * Reference to secret containing domain administrator username and password. Secret must contain
   * keys names 'username' and 'password' (Required)
   */
  @SerializedName("adminSecret")
  @Expose
  @Valid
  @NotNull
  private V1SecretReference adminSecret;

  /**
   * Admin server name. Note: Possibly temporary as we could find this value through domain home
   * inspection. (Required)
   */
  @SerializedName("asName")
  @Expose
  @NotNull
  private String asName;

  /**
   * Administration server port. Note: Possibly temporary as we could find this value through domain
   * home inspection. (Required)
   */
  @SerializedName("asPort")
  @Expose
  @NotNull
  private Integer asPort;

  /**
   * The WebLogic Docker image.
   *
   * <p>Defaults to store/oracle/weblogic:12.2.1.3.
   */
  @Description(
      "The Weblogic Docker image; required when domainHomeInImage is true; "
          + "otherwise, defaults to store/oracle/weblogic:12.2.1.3")
  @SerializedName("image")
  @Expose
  private String image;

  /**
   * The image pull policy for the WebLogic Docker image. Legal values are Always, Never and
   * IfNotPresent.
   *
   * <p>Defaults to Always if image ends in :latest, IfNotPresent otherwise.
   *
   * <p>More info: https://kubernetes.io/docs/concepts/containers/images#updating-images
   */
  @Description(
      "The image pull policy for the WebLogic Docker image. "
          + ""
          + "Legal values are Always, Never and IfNotPresent. "
          + "Defaults to Always if image ends in :latest, IfNotPresent otherwise")
  @SerializedName("imagePullPolicy")
  @Expose
  private String imagePullPolicy;

  /**
   * The image pull secret for the WebLogic Docker image.
   *
   * @deprecated as 2.0, use #imagePullSecrets
   */
  @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
  @Deprecated
  @SerializedName("imagePullSecret")
  @Expose
  private V1LocalObjectReference imagePullSecret;

  /**
   * The image pull secrets for the WebLogic Docker image.
   *
   * <p>More info:
   * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.10/#localobjectreference-v1-core
   *
   * @since 2.0
   */
  @Description("A list of image pull secrets for the WebLogic Docker image.")
  @SerializedName("imagePullSecrets")
  @Expose
  private List<V1LocalObjectReference> imagePullSecrets;

  /**
   * List of specific T3 channels to export. Named T3 Channels will be exposed using NodePort
   * Services. The internal and external ports must match; therefore, it is required that the
   * channel's port in the WebLogic configuration be a legal and unique value in the Kubernetes
   * cluster's legal NodePort port range.
   *
   * @deprecated in 2.0
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @SerializedName("exportT3Channels")
  @Expose
  @Valid
  @Deprecated
  private List<String> exportT3Channels = new ArrayList<>();

  /**
   * The desired number of running managed servers in each WebLogic cluster that is not explicitly
   * configured in a cluster specification.
   */
  @SerializedName("replicas")
  @Expose
  private Integer replicas;

  /**
   * Whether the domain home is part of the image.
   *
   * @since 2.0
   */
  @SerializedName("domainHomeInImage")
  @Expose
  private boolean domainHomeInImage;

  /** The definition of the storage used for this domain. */
  @SerializedName("storage")
  @Expose
  @Description(
      "The storage used for this domain. "
          + "Defaults to a predefined claim for a PVC whose name is "
          + "the domain UID followed by '-weblogic-domain-pvc'")
  private DomainStorage storage;

  /**
   * The configuration for the admin server.
   *
   * @since 2.0
   */
  @SerializedName("adminServer")
  @Expose
  @Description("Configuration for the admin server")
  private AdminServer adminServer;

  /**
   * The configured managed servers.
   *
   * @since 2.0
   */
  @SerializedName("managedServers")
  @Expose
  @Description("Configuration for the managed servers")
  private List<ManagedServer> managedServers = new ArrayList<>();

  /**
   * The configured clusters.
   *
   * @since 2.0
   */
  @SerializedName("clusters")
  @Expose
  @Description("Configuration for the clusters")
  protected List<Cluster> clusters = new ArrayList<>();

  public AdminServer getOrCreateAdminServer(String adminServerName) {
    if (adminServer != null) return adminServer;

    return createAdminServer(adminServerName);
  }

  private AdminServer createAdminServer(String adminServerName) {
    setAsName(adminServerName);
    AdminServer adminServer = new AdminServer();
    setAdminServer(adminServer);
    return adminServer;
  }

  EffectiveConfigurationFactory getEffectiveConfigurationFactory(String resourceVersionLabel) {
    return new V2EffectiveConfigurationFactory();
  }

  private boolean isVersion2Specified(String resourceVersionLabel) {
    return VersionConstants.DOMAIN_V2.equals(resourceVersionLabel);
  }

  /**
   * Domain unique identifier. Must be unique across the Kubernetes cluster. (Required)
   *
   * @return domain UID
   */
  public String getDomainUID() {
    return domainUID;
  }

  /**
   * Domain unique identifier. Must be unique across the Kubernetes cluster. (Required)
   *
   * @param domainUID domain UID
   */
  public void setDomainUID(String domainUID) {
    this.domainUID = domainUID;
  }

  /**
   * Domain unique identifier. Must be unique across the Kubernetes cluster. (Required)
   *
   * @param domainUID domain UID
   * @return this
   */
  public DomainSpec withDomainUID(String domainUID) {
    this.domainUID = domainUID;
    return this;
  }

  /**
   * Domain name (Required)
   *
   * @return domain name
   */
  public String getDomainName() {
    return domainName;
  }

  /**
   * Domain name (Required)
   *
   * @param domainName domain name
   */
  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  /**
   * Domain name (Required)
   *
   * @param domainName domain name
   * @return this
   */
  public DomainSpec withDomainName(String domainName) {
    this.domainName = domainName;
    return this;
  }

  /*
   * Fluent api for setting the image.
   *
   * @param image image
   * @return this
   */
  public DomainSpec withImage(String image) {
    setImage(image);
    return this;
  }

  /**
   * The image pull policy for the WebLogic Docker image. Legal values are Always, Never and
   * IfNotPresent.
   *
   * <p>Defaults to Always if image ends in :latest, IfNotPresent otherwise.
   *
   * <p>More info: https://kubernetes.io/docs/concepts/containers/images#updating-images
   *
   * @param imagePullPolicy image pull policy
   * @return this
   */
  public DomainSpec withImagePullPolicy(String imagePullPolicy) {
    setImagePullPolicy(imagePullPolicy);
    return this;
  }

  /**
   * The name of the secret used to authenticate a request for an image pull.
   *
   * <p>More info:
   * https://kubernetes.io/docs/concepts/containers/images/#referring-to-an-imagepullsecrets-on-a-pod
   */
  public DomainSpec withImagePullSecretName(String imagePullSecretName) {
    setImagePullSecret(new V1LocalObjectReference().name(imagePullSecretName));
    return this;
  }

  public V1SecretReference getAdminSecret() {
    return adminSecret;
  }

  @SuppressWarnings("unused")
  public void setAdminSecret(V1SecretReference adminSecret) {
    this.adminSecret = adminSecret;
  }

  /**
   * Reference to secret containing domain administrator username and password. Secret must contain
   * keys names 'username' and 'password' (Required)
   *
   * @param adminSecret admin secret
   * @return this
   */
  public DomainSpec withAdminSecret(V1SecretReference adminSecret) {
    this.adminSecret = adminSecret;
    return this;
  }

  public String getAsName() {
    return asName;
  }

  public void setAsName(String asName) {
    this.asName = asName;
  }

  /**
   * Admin server name. Note: Possibly temporary as we could find this value through domain home
   * inspection. (Required)
   *
   * @param asName admin server name
   * @return this
   */
  public DomainSpec withAsName(String asName) {
    this.asName = asName;
    return this;
  }

  public Integer getAsPort() {
    return asPort;
  }

  @SuppressWarnings("WeakerAccess")
  // public access is needed for yaml processing
  public void setAsPort(Integer asPort) {
    this.asPort = asPort;
  }

  /**
   * Administration server port. Note: Possibly temporary as we could find this value through domain
   * home inspection. (Required)
   *
   * @param asPort admin server port
   * @return this
   */
  public DomainSpec withAsPort(Integer asPort) {
    this.asPort = asPort;
    return this;
  }

  @Nullable
  public String getImage() {
    return image;
  }

  public void setImage(@Nullable String image) {
    this.image = image;
  }

  @Nullable
  public String getImagePullPolicy() {
    return imagePullPolicy;
  }

  public void setImagePullPolicy(@Nullable String imagePullPolicy) {
    this.imagePullPolicy = imagePullPolicy;
  }

  @Nullable
  V1LocalObjectReference getImagePullSecret() {
    if (isDefined(imagePullSecret)) return imagePullSecret;

    return !hasImagePullSecrets() ? null : getReturnValue(imagePullSecrets.get(0));
  }

  private boolean hasImagePullSecrets() {
    return imagePullSecrets != null && imagePullSecrets.size() != 0;
  }

  @Nullable
  public List<V1LocalObjectReference> getImagePullSecrets() {
    if (hasImagePullSecrets()) return imagePullSecrets;
    else if (isDefined(imagePullSecret)) return Collections.singletonList(imagePullSecret);
    else return Collections.emptyList();
  }

  private V1LocalObjectReference getReturnValue(V1LocalObjectReference imagePullSecret) {
    return isDefined(imagePullSecret) ? imagePullSecret : null;
  }

  private boolean isDefined(V1LocalObjectReference imagePullSecret) {
    return imagePullSecret != null && !Strings.isNullOrEmpty(imagePullSecret.getName());
  }

  public void setImagePullSecret(@Nullable V1LocalObjectReference imagePullSecret) {
    imagePullSecrets = Collections.singletonList(imagePullSecret);
  }

  public void setImagePullSecrets(@Nullable List<V1LocalObjectReference> imagePullSecrets) {
    this.imagePullSecrets = imagePullSecrets;
  }

  /**
   * List of specific T3 channels to export. Named T3 Channels will be exposed using NodePort
   * Services. The internal and external ports must match; therefore, it is required that the
   * channel's port in the WebLogic configuration be a legal and unique value in the Kubernetes
   * cluster's legal NodePort port range.
   *
   * @return exported channels
   */
  List<String> getExportT3Channels() {
    return exportT3Channels;
  }

  /**
   * List of specific T3 channels to export. Named T3 Channels will be exposed using NodePort
   * Services. The internal and external ports must match; therefore, it is required that the
   * channel's port in the WebLogic configuration be a legal and unique value in the Kubernetes
   * cluster's legal NodePort port range.
   *
   * @param exportT3Channels exported channels
   */
  void setExportT3Channels(List<String> exportT3Channels) {
    this.exportT3Channels = exportT3Channels;
  }

  /**
   * List of specific T3 channels to export. Named T3 Channels will be exposed using NodePort
   * Services. The internal and external ports must match; therefore, it is required that the
   * channel's port in the WebLogic configuration be a legal and unique value in the Kubernetes
   * cluster's legal NodePort port range.
   *
   * @param exportT3Channels exported channels
   * @return this
   */
  @SuppressWarnings("UnusedReturnValue")
  public DomainSpec withExportT3Channels(List<String> exportT3Channels) {
    this.exportT3Channels = exportT3Channels;
    return this;
  }

  /**
   * Returns true if this domain's home is defined in the default docker image for the domain.
   *
   * @return true or false
   * @since 2.0
   */
  public boolean isDomainHomeInImage() {
    return domainHomeInImage;
  }

  /**
   * Replicas is the desired number of managed servers running in each WebLogic cluster that is not
   * configured under clusterStartup. Provided so that administrators can scale the Domain resource.
   *
   * @deprecated use {@link Domain#getReplicaCount(String)} to obtain the effective setting.
   * @return replicas
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated
  public Integer getReplicas() {
    return replicas != null ? replicas : 0;
  }

  /**
   * The desired number of running managed servers in each WebLogic cluster that is not explicitly
   * configured in clusterStartup.
   *
   * @param replicas replicas
   */
  @SuppressWarnings("deprecation")
  public void setReplicas(Integer replicas) {
    this.replicas = replicas;
  }

  /**
   * The desired number of running managed servers in each WebLogic cluster that is not explicitly
   * configured in clusterStartup.
   *
   * @param replicas replicas
   * @return this
   */
  @SuppressWarnings("deprecation")
  public DomainSpec withReplicas(Integer replicas) {
    this.replicas = replicas;
    return this;
  }

  /**
   * Specifies the storage for the domain.
   *
   * @param storage the definition of the domain storage.
   */
  public void setStorage(DomainStorage storage) {
    this.storage = storage;
  }

  /**
   * Returns the storage for the domain.
   *
   * @return the definition of the domain storage.
   */
  public DomainStorage getStorage() {
    return storage;
  }

  /**
   * Returns the name of the persistent volume claim for the logs and PV-based domain.
   *
   * @return volume claim
   */
  String getPersistentVolumeClaimName() {
    return storage == null ? null : getConfiguredClaimName(storage);
  }

  private String getConfiguredClaimName(@Nonnull DomainStorage storage) {
    return Optional.ofNullable(storage.getPersistentVolumeClaimName())
        .orElse(String.format(PVC_NAME_PATTERN, domainUID));
  }

  @Nullable
  @Override
  protected String getServerStartPolicy() {
    return Optional.ofNullable(super.getServerStartPolicy()).orElse(START_IF_NEEDED);
  }

  @SuppressWarnings("deprecation")
  @Override
  public String toString() {
    ToStringBuilder builder =
        new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("domainUID", domainUID)
            .append("domainName", domainName)
            .append("adminSecret", adminSecret)
            .append("asName", asName)
            .append("asPort", asPort)
            .append("image", image)
            .append("imagePullPolicy", imagePullPolicy)
            .append("storage", storage);

    builder
        .append("imagePullSecrets", imagePullSecrets)
        .append("adminServer", adminServer)
        .append("managedServers", managedServers)
        .append("clusters", clusters);

    return builder.toString();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int hashCode() {
    HashCodeBuilder builder =
        new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(domainUID)
            .append(domainName)
            .append(adminSecret)
            .append(asName)
            .append(asPort)
            .append(image)
            .append(imagePullPolicy)
            .append(storage);

    builder.append(imagePullSecrets).append(adminServer).append(managedServers).append(clusters);

    return builder.toHashCode();
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof DomainSpec)) return false;

    DomainSpec rhs = ((DomainSpec) other);
    EqualsBuilder builder =
        new EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(domainUID, rhs.domainUID)
            .append(domainName, rhs.domainName)
            .append(adminSecret, rhs.adminSecret)
            .append(asName, rhs.asName)
            .append(asPort, rhs.asPort)
            .append(image, rhs.image)
            .append(storage, rhs.storage)
            .append(imagePullPolicy, rhs.imagePullPolicy);

    builder
        .append(imagePullSecrets, rhs.imagePullSecrets)
        .append(adminServer, rhs.adminServer)
        .append(managedServers, rhs.managedServers)
        .append(clusters, rhs.clusters);

    return builder.isEquals();
  }

  private Server getServer(String serverName) {
    for (ManagedServer managedServer : managedServers) {
      if (Objects.equals(serverName, managedServer.getServerName())) return managedServer;
    }

    return null;
  }

  private Cluster getCluster(String clusterName) {
    for (Cluster cluster : clusters) {
      if (Objects.equals(clusterName, cluster.getClusterName())) return cluster;
    }

    return null;
  }

  private int getReplicaCountFor(Cluster cluster) {
    return hasReplicaCount(cluster)
        ? cluster.getReplicas()
        : Optional.ofNullable(replicas).orElse(0);
  }

  private boolean hasReplicaCount(Cluster cluster) {
    return cluster != null && cluster.getReplicas() != null;
  }

  public AdminServer getAdminServer() {
    return Optional.ofNullable(adminServer).orElse(AdminServer.NULL_ADMIN_SERVER);
  }

  public void setAdminServer(AdminServer adminServer) {
    this.adminServer = adminServer;
  }

  public List<ManagedServer> getManagedServers() {
    return managedServers;
  }

  public List<Cluster> getClusters() {
    return clusters;
  }

  class V2EffectiveConfigurationFactory implements EffectiveConfigurationFactory {
    @Override
    public ServerSpec getAdminServerSpec() {
      return new AdminServerSpecV2Impl(DomainSpec.this, adminServer);
    }

    @Override
    public ServerSpec getServerSpec(String serverName, String clusterName) {
      return new ManagedServerSpecV2Impl(
          DomainSpec.this,
          getServer(serverName),
          getClusterLimit(clusterName),
          getCluster(clusterName),
          DomainSpec.this);
    }

    private Integer getClusterLimit(String clusterName) {
      return clusterName == null ? null : getReplicaCount(clusterName);
    }

    @Override
    public boolean isShuttingDown() {
      return !getAdminServerSpec().shouldStart(0);
    }

    @Override
    public int getReplicaCount(String clusterName) {
      return getReplicaCountFor(getCluster(clusterName));
    }

    @Override
    public void setReplicaCount(String clusterName, int replicaCount) {
      getOrCreateCluster(clusterName).setReplicas(replicaCount);
    }

    @Override
    public List<String> getExportedNetworkAccessPointNames() {
      return getAdminServer().getExportedNetworkAccessPointNames();
    }

    @Override
    public Map<String, String> getChannelServiceLabels(String napName) {
      ExportedNetworkAccessPoint accessPoint = getExportedNetworkAccessPoint(napName);

      return accessPoint == null ? Collections.emptyMap() : accessPoint.getLabels();
    }

    private ExportedNetworkAccessPoint getExportedNetworkAccessPoint(String napName) {
      return getAdminServer().getExportedNetworkAccessPoint(napName);
    }

    @Override
    public Map<String, String> getChannelServiceAnnotations(String napName) {
      ExportedNetworkAccessPoint accessPoint = getExportedNetworkAccessPoint(napName);

      return accessPoint == null ? Collections.emptyMap() : accessPoint.getAnnotations();
    }

    @Override
    public Integer getDefaultReplicaLimit() {
      return 0;
    }

    private Cluster getOrCreateCluster(String clusterName) {
      Cluster cluster = getCluster(clusterName);
      if (cluster != null) return cluster;

      return createClusterWithName(clusterName);
    }

    private Cluster createClusterWithName(String clusterName) {
      Cluster cluster = new Cluster().withClusterName(clusterName);
      clusters.add(cluster);
      return cluster;
    }
  }
}
