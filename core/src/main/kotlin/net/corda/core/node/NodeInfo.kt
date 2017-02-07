package net.corda.core.node

import net.corda.core.crypto.Party
import net.corda.core.flows.AdvertisedFlow
import net.corda.core.messaging.SingleMessageRecipient
import net.corda.core.node.services.ServiceInfo
import net.corda.core.node.services.ServiceType

/**
 * Information for an advertised service including the service specific identity information.
 * The identity can be used in flows and is distinct from the Node's legalIdentity
 */
// TODO It needs refactor ServiceEntry, ServiceInfo, NodeInfo - it got messy.
data class ServiceEntry(val info: ServiceInfo, val identity: Party)

/**
 * Info about a network node that acts on behalf of some form of contract party.
 */
data class NodeInfo(val address: SingleMessageRecipient,
                    val legalIdentity: Party,
                    var advertisedServices: List<ServiceEntry> = emptyList(),
                    // Flows advertised by this node as peer flows, opposed to service flows.
                    var advertisedPeerFlows: List<AdvertisedFlow> = emptyList(),
                    val physicalLocation: PhysicalLocation? = null) {
    val notaryIdentity: Party get() = advertisedServices.single { it.info.type.isNotary() }.identity
    init {
        require((advertisedServices.distinctBy { it.identity.owningKey }).size == advertisedServices.size) { "All services should have different owning keys" }
        // TODO Sanity check. Change to Map, overload plus on AdvertisedServices.
        require(advertisedPeerFlows.groupBy { it.genericFlowName }.size == advertisedPeerFlows.size)
    }
    fun serviceIdentities(type: ServiceType): List<Party> = advertisedServices.filter { it.info.type.isSubTypeOf(type) }.map { it.identity }
}
