package auth.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration._

object OidcLoginTransactionActor {

    sealed trait Command
    final case class Store(transaction: OidcLoginTransaction, replyTo: ActorRef[StoreResult]) extends Command
    final case class Consume(state: String, replyTo: ActorRef[ConsumeResult]) extends Command
    private final case class Expire(state: String) extends Command

    sealed trait StoreResult
    case object Stored extends StoreResult
    case object AlreadyExists extends StoreResult

    final case class ConsumeResult(transaction: Option[OidcLoginTransaction])

    def apply(ttl: FiniteDuration = 5.minutes): Behavior[Command] = Behaviors.withTimers { timers =>
        def active(transactions: Map[String, OidcLoginTransaction]): Behavior[Command] =
            Behaviors.receiveMessage {

                case Store(transaction, replyTo) if transactions.contains(transaction.state) =>
                    replyTo ! AlreadyExists
                    Behaviors.same

                case Store(transaction, replyTo) =>
                    timers.startSingleTimer(transaction.state, Expire(transaction.state), ttl)
                    replyTo ! Stored
                    active(transactions.updated(transaction.state, transaction))

                case Consume(state, replyTo) =>
                    timers.cancel(state)
                    replyTo ! ConsumeResult(transactions.get(state))
                    active(transactions - state)

                case Expire(state) =>
                    active(transactions - state)
            }

        active(Map.empty)
    }
}