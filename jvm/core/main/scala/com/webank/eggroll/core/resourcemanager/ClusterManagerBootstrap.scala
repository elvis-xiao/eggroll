package com.webank.eggroll.core.resourcemanager

import com.webank.eggroll.core.BootstrapBase
import com.webank.eggroll.core.command.{CommandRouter, CommandService}
import com.webank.eggroll.core.constant._
import com.webank.eggroll.core.deepspeed.job.JobServiceHandler
import com.webank.eggroll.core.deepspeed.store.RendezvousStoreService
import com.webank.eggroll.core.meta._
import com.webank.eggroll.core.resourcemanager.metadata.{ServerNodeCrudOperator, StoreCrudOperator}
import com.webank.eggroll.core.session.StaticErConf
import com.webank.eggroll.core.transfer.{GrpcClientUtils, GrpcServerUtils}
import com.webank.eggroll.core.util.{CommandArgsUtils, Logging}
import org.apache.commons.lang3.StringUtils

import java.io.File

class ClusterManagerBootstrap extends BootstrapBase with Logging {
  private var port = 0
  private var standaloneTag = "0"

  //private var sessionId = "er_session_null"
  override def init(args: Array[String]): Unit = {

    CommandRouter.register(serviceName = MetadataCommands.getServerNodeServiceName,
      serviceParamTypes = Array(classOf[ErServerNode]),
      serviceResultTypes = Array(classOf[ErServerNode]),
      routeToClass = classOf[ServerNodeCrudOperator],
      routeToMethodName = MetadataCommands.getServerNode)

    CommandRouter.register(serviceName = MetadataCommands.getServerNodesServiceName,
      serviceParamTypes = Array(classOf[ErServerNode]),
      serviceResultTypes = Array(classOf[ErServerCluster]),
      routeToClass = classOf[ServerNodeCrudOperator],
      routeToMethodName = MetadataCommands.getServerNodes)

    CommandRouter.register(serviceName = MetadataCommands.getOrCreateServerNodeServiceName,
      serviceParamTypes = Array(classOf[ErServerNode]),
      serviceResultTypes = Array(classOf[ErServerNode]),
      routeToClass = classOf[ServerNodeCrudOperator],
      routeToMethodName = MetadataCommands.getOrCreateServerNode)

    CommandRouter.register(serviceName = MetadataCommands.createOrUpdateServerNodeServiceName,
      serviceParamTypes = Array(classOf[ErServerNode]),
      serviceResultTypes = Array(classOf[ErServerNode]),
      routeToClass = classOf[ServerNodeCrudOperator],
      routeToMethodName = MetadataCommands.createOrUpdateServerNode)

    CommandRouter.register(serviceName = MetadataCommands.getStoreServiceName,
      serviceParamTypes = Array(classOf[ErStore]),
      serviceResultTypes = Array(classOf[ErStore]),
      routeToClass = classOf[StoreCrudOperator],
      routeToMethodName = MetadataCommands.getStore)

    CommandRouter.register(serviceName = MetadataCommands.getOrCreateStoreServiceName,
      serviceParamTypes = Array(classOf[ErStore]),
      serviceResultTypes = Array(classOf[ErStore]),
      routeToClass = classOf[StoreCrudOperator],
      routeToMethodName = MetadataCommands.getOrCreateStore)

    CommandRouter.register(serviceName = MetadataCommands.deleteStoreServiceName,
      serviceParamTypes = Array(classOf[ErStore]),
      serviceResultTypes = Array(classOf[ErStore]),
      routeToClass = classOf[StoreCrudOperator],
      routeToMethodName = MetadataCommands.deleteStore)

    CommandRouter.register(serviceName = MetadataCommands.getStoreFromNamespaceServiceName,
      serviceParamTypes = Array(classOf[ErStore]),
      serviceResultTypes = Array(classOf[ErStoreList]),
      routeToClass = classOf[StoreCrudOperator],
      routeToMethodName = MetadataCommands.getStoreFromNamespace)

    CommandRouter.register(serviceName = SessionCommands.getSession.uriString,
      serviceParamTypes = Array(classOf[ErSessionMeta]),
      serviceResultTypes = Array(classOf[ErSessionMeta]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.getSession.getName())

    CommandRouter.register(serviceName = SessionCommands.getOrCreateSession.uriString,
      serviceParamTypes = Array(classOf[ErSessionMeta]),
      serviceResultTypes = Array(classOf[ErSessionMeta]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.getOrCreateSession.getName())

    CommandRouter.register(serviceName = SessionCommands.stopSession.uriString,
      serviceParamTypes = Array(classOf[ErSessionMeta]),
      serviceResultTypes = Array(classOf[ErSessionMeta]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.stopSession.getName())

    CommandRouter.register(serviceName = SessionCommands.killSession.uriString,
      serviceParamTypes = Array(classOf[ErSessionMeta]),
      serviceResultTypes = Array(classOf[ErSessionMeta]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.killSession.getName())

    CommandRouter.register(serviceName = SessionCommands.killAllSessions.uriString,
      serviceParamTypes = Array(classOf[ErSessionMeta]),
      serviceResultTypes = Array(classOf[ErSessionMeta]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.killAllSessions.getName())

    CommandRouter.register(serviceName = SessionCommands.registerSession.uriString,
      serviceParamTypes = Array(classOf[ErSessionMeta]),
      serviceResultTypes = Array(classOf[ErSessionMeta]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.registerSession.getName())

    CommandRouter.register(serviceName = SessionCommands.heartbeat.uriString,
      serviceParamTypes = Array(classOf[ErProcessor]),
      serviceResultTypes = Array(classOf[ErProcessor]),
      routeToClass = classOf[SessionManagerService],
      routeToMethodName = SessionCommands.heartbeat.getName())

    CommandRouter.register(serviceName = ManagerCommands.nodeHeartbeat.uriString,
      serviceParamTypes = Array(classOf[ErNodeHeartbeat]),
      serviceResultTypes = Array(classOf[ErNodeHeartbeat]),
      routeToClass = classOf[ClusterManagerService],
      routeToMethodName = ManagerCommands.nodeHeartbeat.getName())

    CommandRouter.register(serviceName = ManagerCommands.registerResource.uriString,
      serviceParamTypes = Array(classOf[ErServerNode]),
      serviceResultTypes = Array(classOf[ErServerNode]),
      routeToClass = classOf[ClusterManagerService],
      routeToMethodName = ManagerCommands.registerResource.getName())


    // submit job
    //JobServiceHandler.startSessionWatcher()
    CommandRouter.register_handler(serviceName = JobCommands.submitJob.uriString,
      args => JobServiceHandler.handleSubmit(args(0))
    )
    CommandRouter.register_handler(serviceName = JobCommands.queryJobStatus.uriString,
      args => JobServiceHandler.handleJobStatusQuery(args(0))
    )
    CommandRouter.register_handler(serviceName = JobCommands.queryJob.uriString,
      args => JobServiceHandler.handleJobQuery(args(0))
    )
    CommandRouter.register_handler(serviceName = JobCommands.killJob.uriString,
      args => JobServiceHandler.handleJobKill(args(0))
    )
    CommandRouter.register_handler(serviceName = JobCommands.stopJob.uriString,
      args => JobServiceHandler.handleJobStop(args(0))
    )
    CommandRouter.register_handler(serviceName = JobCommands.downloadJob.uriString,
      args => JobServiceHandler.handleJobDownload(args(0))
    )

    val rendezvousStoreService = new RendezvousStoreService()
    CommandRouter.register_handler(serviceName = RendezvousStoreCommands.set.uriString,
      args => rendezvousStoreService.set(args(0))
    )
    CommandRouter.register_handler(serviceName = RendezvousStoreCommands.get.uriString,
      args => rendezvousStoreService.get(args(0))
    )
    CommandRouter.register_handler(serviceName = RendezvousStoreCommands.add.uriString,
      args => rendezvousStoreService.add(args(0))
    )
    CommandRouter.register_handler(serviceName = RendezvousStoreCommands.destroy.uriString,
      args => rendezvousStoreService.destroy(args(0))
    )

    val cmd = CommandArgsUtils.parseArgs(args = args)

    //this.sessionId = cmd.getOptionValue('s')
    val confPath = cmd.getOptionValue('c', "./conf/eggroll.properties")
    standaloneTag = System.getProperty("eggroll.standalone.tag", "")

    StaticErConf.addProperties(confPath)
    val confFile = new File(confPath)
    StaticErConf.addProperty(CoreConfKeys.STATIC_CONF_PATH, confFile.getAbsolutePath)
    logInfo(s"conf file: ${confFile.getAbsolutePath}")
    this.port = cmd.getOptionValue('p', cmd.getOptionValue('p', StaticErConf.getProperty(
      ClusterManagerConfKeys.CONFKEY_CLUSTER_MANAGER_PORT, "4670"))).toInt

    if (StringUtils.isBlank(standaloneTag)) {
      Runtime.getRuntime.addShutdownHook(new Thread(() => {
        logWarning("****** Shutting down Cluster Manager ******")
        logInfo("Shutting down cluster manager. Force terminating all grpc channel")
        GrpcClientUtils.shutdownNow()
        logInfo("All grpc client channels are shut down")
        logInfo("Shutting down cluster manager. Force terminating NEW / ACTIVE sessions")
        val sessionManagerService = new SessionManagerService()
        sessionManagerService.killAllSessions(ErSessionMeta())

        logWarning("****** All sessions stopped / killed. Cluster Manager exiting gracefully ******")
      }))
    }
    //StaticErConf.addProperty(SessionConfKeys.CONFKEY_SESSION_ID, sessionId)
  }

  override def start(): Unit = {
    // TODO:0: use user's config
    val server = GrpcServerUtils.createServer(port = this.port, grpcServices = List(new CommandService))
    server.start()
    this.port = server.getPort

    StaticErConf.setPort(port)
    logInfo(s"$standaloneTag server started at port $port")
    println(s"$standaloneTag server started at port $port")
    ClusterResourceManager.start()
    ClusterManagerService.start()
  }
}
