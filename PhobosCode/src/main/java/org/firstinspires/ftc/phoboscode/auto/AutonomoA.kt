package org.firstinspires.ftc.phoboscode.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.github.serivesmejia.deltacommander.dsl.deltaSequence
import com.github.serivesmejia.deltacommander.endRightAway
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.firstinspires.ftc.phoboscode.Alliance
import org.firstinspires.ftc.phoboscode.command.intake.*
import org.firstinspires.ftc.phoboscode.command.lift.LiftMoveDownCmd
import org.firstinspires.ftc.phoboscode.command.lift.LiftMoveToHighCmd
import org.firstinspires.ftc.phoboscode.command.lift.LiftMoveToPosCmd
import org.firstinspires.ftc.phoboscode.command.turret.TurretMoveToAngleCmd
import org.firstinspires.ftc.phoboscode.rr.drive.DriveConstants
import org.firstinspires.ftc.phoboscode.rr.drive.SampleMecanumDrive
import org.firstinspires.ftc.phoboscode.rr.trajectorysequence.TrajectorySequenceBuilder
import org.firstinspires.ftc.phoboscode.subsystem.Lift
import org.firstinspires.ftc.phoboscode.vision.SleevePattern
import org.firstinspires.ftc.phoboscode.vision.SleevePattern.*

abstract class AutonomoA(
    alliance: Alliance,
    val cycles: Int = 5
) : AutonomoBase(alliance) {

    override val startPose = Pose2d(-35.0, -58.0, Math.toRadians(90.0))

    override fun sequence(sleevePattern: SleevePattern) = drive.trajectorySequenceBuilder(startPose).apply {
        UNSTABLE_addTemporalMarkerOffset(0.0) {
            + IntakeArmPositionSaveCmd()
        }

        // prepare for putting preload cone
        UNSTABLE_addTemporalMarkerOffset(0.55) { + prepareForPuttingCone(-90.0, Lift.highPos - 150) }
        UNSTABLE_addTemporalMarkerOffset(1.75) {
            + IntakeArmAndTiltCmd(0.65, 0.58)
        }
        lineToConstantHeading(Vector2d(-35.7, 2.9)) // TODO: Preload cone score position

        UNSTABLE_addTemporalMarkerOffset(0.003) { + IntakeWheelsReleaseCmd() }
        waitSeconds(0.24)

        var liftHeight = 485.0 // TODO: altura de los rieles

        UNSTABLE_addTemporalMarkerOffset(0.1) {
            + IntakeArmPositionSaveCmd()
            + IntakeWheelsStopCmd()

            + LiftMoveToPosCmd(liftHeight + 365)
            + TurretMoveToAngleCmd(95.0)
        }

        // just park here when we won`t be doing any cycles
        if(cycles == 0) {
            // park
            when(sleevePattern) {
                A -> {
                    lineToSplineHeading(Pose2d(-35.0, -33.0, Math.toRadians(0.0)))

                    setReversed(true)
                    splineToConstantHeading(Vector2d(-58.0, -35.0), Math.toRadians(180.0))
                    setReversed(false)
                }
                B -> {
                    lineToLinearHeading(Pose2d(-35.0, -35.0, Math.toRadians(0.0)))
                }
                C -> {
                    lineToSplineHeading(Pose2d(-35.0, -33.0, Math.toRadians(0.0)))
                    lineToConstantHeading(Vector2d(11.5, -35.0))

                    turn(Math.toRadians(-90.0))
                }
            }

            return@apply
        }

        val grabX = -57.8 // TODO: Grab coordinates
        var grabY = -6.3

        setReversed(true)
        splineToConstantHeading(Vector2d(-45.0, grabY), Math.toRadians(180.0))

        UNSTABLE_addTemporalMarkerOffset(0.2) {
            + IntakeArmAndZeroTiltCmd(0.43)
            + IntakeWheelsAbsorbCmd()
        }

        UNSTABLE_addTemporalMarkerOffset(0.4) {
            + IntakeArmPositionCmd(0.4)
        }

        lineToConstantHeading(Vector2d(grabX, grabY))
        setReversed(false)

        waitSeconds(0.7)

        repeat(cycles - 1) {
            liftHeight -= 130

            putOnHigh(95.0, liftHeight)

            UNSTABLE_addTemporalMarkerOffset(0.8) {
                + IntakeWheelsAbsorbCmd()
            }

            val armPosition = if(cycles == 5 && it == cycles - 1) {
                0.41
            } else 0.45

            UNSTABLE_addTemporalMarkerOffset(1.0) {
                + IntakeArmAndZeroTiltCmd(armPosition)
            }

            UNSTABLE_addTemporalMarkerOffset(1.3) {
                + IntakeArmPositionCmd(armPosition - 0.04)
            }

            lineToSplineHeading(Pose2d(grabX, grabY, Math.toRadians(90.0)))

            waitSeconds(0.3)

            grabY -= 0.02
        }

        putOnHigh(endingLiftPos = 0.0, endingTurretAngle = 0.0)

        when(sleevePattern) {
            A -> {
                lineToLinearHeading(Pose2d(-11.0, -7.7, Math.toRadians(90.0)))
            }
            B -> {
                lineToLinearHeading(Pose2d(-34.5, -7.3, Math.toRadians(90.0)))
            }
            C -> {
                lineToLinearHeading(Pose2d(-60.0, -7.3, Math.toRadians(90.0)))
            }
        }

        waitSeconds(5.0)

        // besito de la suerte
    }.build()

    fun prepareForPuttingCone(turretAngle: Double, liftPos: Int = Lift.highPos) = deltaSequence {
        - LiftMoveToPosCmd(liftPos.toDouble()).dontBlock()

        - waitForSeconds(0.1)

        - TurretMoveToAngleCmd(turretAngle).dontBlock()
    }

    private var putOnHighX = -28.15

    fun TrajectorySequenceBuilder.putOnHigh(endingTurretAngle: Double, endingLiftPos: Double? = null) {
        UNSTABLE_addTemporalMarkerOffset(0.0) {
            + IntakeArmPositionSaveCmd()
            + IntakeWheelsHoldCmd()
        }
        UNSTABLE_addTemporalMarkerOffset(0.2) { // TODO: tiempo para que se mueva la torreta
            + prepareForPuttingCone(-35.0, Lift.highPos - 40)
        }

        UNSTABLE_addTemporalMarkerOffset(1.3) {
            + IntakeArmAndTiltCmd(0.53, 0.62) // TODO: score position of intake arm
        }
        lineToConstantHeading(Vector2d(putOnHighX, -6.28)) // TODO: high pole coordinates

        UNSTABLE_addTemporalMarkerOffset(0.0005) {
            + IntakeWheelsReleaseCmd()
        }

        UNSTABLE_addTemporalMarkerOffset(0.25) {
            + IntakeArmPositionSaveCmd()

            + LiftMoveToPosCmd(endingLiftPos ?: Lift.lowPos.toDouble())
            + TurretMoveToAngleCmd(endingTurretAngle)
        }

        UNSTABLE_addTemporalMarkerOffset(0.3) {
            + IntakeWheelsStopCmd()
        }

        waitSeconds(0.27)

        putOnHighX += 0.04
    }

}