package app.dao

import app.model.Entity
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory
import org.neo4j.ogm.transaction.Transaction

object CompetitionGraph {

    private val ogmConfiguration = buildOgmConfiguration {
        uri("bolt://localhost")
        credentials("neo4j", "demo")
    }

    val sessionFactory = SessionFactory(ogmConfiguration, Entity::class.java.`package`.name)

    inline fun <R> session(action: Session.() -> R): R = sessionFactory.openSession().run(action)

    inline fun <R> transaction(type: Transaction.Type, action: Session.() -> R): R {
        session {
            val transaction = beginTransaction(type)
            try {
                val result = action()
                transaction.commit()
                return result
            } catch (e: Throwable) {
                transaction.rollback()
                throw e
            }
        }
    }

    inline fun <R> readOnlyTransaction(action: Session.() -> R) = transaction(Transaction.Type.READ_ONLY, action)
    inline fun <R> readWriteTransaction(action: Session.() -> R) = transaction(Transaction.Type.READ_WRITE, action)

}