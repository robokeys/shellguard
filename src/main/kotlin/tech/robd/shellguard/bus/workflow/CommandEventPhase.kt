package tech.robd.shellguard.bus.workflow
/**
 * [File Info]
 * path: tech/robd/shellguard/bus/workflow/CommandEventPhase.kt
 * description: Enum representing all workflow phases in the command/event lifecycle for RKCL and approval.
 * license: GPL-3.0
 * editable: yes
 * structured: no
 * [/File Info]
 */

/**
 * Command workflow phases - tracks the complete lifecycle
 */
enum class CommandEventPhase {
    SUBMITTED,
    RISK_ASSESSED,
    PENDING_APPROVAL,
    APPROVED, // will be able to move to READY_TO_RUN if it is the first in the queue
    REJECTED,
    IMMEDIATE_EXECUTE_APPROVAL, // this one means it will execute even if there is a pending approval status item in the queue
    READY_TO_RUN, // this will be actually actioned
    EXECUTION_STARTED,
    COMPLETED,
    FAILED,
    OUTPUT
}
