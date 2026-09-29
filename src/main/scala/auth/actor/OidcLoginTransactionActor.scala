package auth.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object OidcLoginTransactionActor {

    sealed trait Command
    final case class Store(transaction: OidcLoginTransaction) extends Command
    final case class Consume(state: String, replyTo: ActorRef[ConsumeResult]) extends Command

    final case class ConsumeResult(transaction: Option[OidcLoginTransaction])

    def apply(): Behavior[Command] = active(Map.empty)

    private def active(transactions: Map[String, OidcLoginTransaction]): Behavior[Command] = Behaviors.receiveMessage {
        case Store(transaction) =>
            active(transactions.updated(transaction.state, transaction))

        case Consume(state, replyTo) =>
            val transaction = transactions.get(state)
            replyTo ! ConsumeResult(transaction)
            active(transactions - state)
    }
}