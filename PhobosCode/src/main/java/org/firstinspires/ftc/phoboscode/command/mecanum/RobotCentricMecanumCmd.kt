package org.firstinspires.ftc.phoboscode.command.mecanum

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.github.serivesmejia.deltacommander.DeltaCommand
import com.github.serivesmejia.deltacommander.subsystem
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.phoboscode.subsystem.MecanumSubsystem

class RobotCentricMecanumCmd(val gamepad: Gamepad, val applyTurboWithTriggers: Boolean = true) : DeltaCommand() {

    val sub = require<MecanumSubsystem>()

    override fun run() {
        val triggerValue = if(gamepad.left_trigger > 0.2) {
            gamepad.left_trigger
        } else gamepad.right_trigger

        val turbo = if(applyTurboWithTriggers) {
            1.0 - (triggerValue * 0.6)
        } else 1.0

        val input = Vector2d(
            (-gamepad.left_stick_y).toDouble() * 0.7 * turbo,
            (-gamepad.left_stick_x).toDouble() * 0.7 * turbo
        )

        sub.drive.setWeightedDrivePower(
            Pose2d(
                (-gamepad.left_stick_y).toDouble() * 0.7 * turbo,
                (-gamepad.left_stick_x).toDouble() * 0.7 * turbo,
                (-gamepad.right_stick_x).toDouble() * 0.7 * turbo
            )
        )
    }

}