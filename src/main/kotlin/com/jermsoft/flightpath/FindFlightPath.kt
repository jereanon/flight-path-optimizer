package com.jermsoft.flightpath

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm
import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory
import com.graphhopper.jsprit.core.problem.Location
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem
import com.graphhopper.jsprit.core.problem.job.Pickup
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
import com.graphhopper.jsprit.core.reporting.SolutionPrinter
import com.graphhopper.jsprit.core.util.Solutions
import java.io.File
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.analysis.toolbox.Plotter.Label;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow

fun createOutputFolder() {
    val dir = File("output")
    if (!dir.exists()) {
        println("creating directory ./output")
        val result = dir.mkdir()
        if (result) println("./output created")
    }
}

fun knotsToMetersPerSecond(knots: Double): Double {
    return knots * .51444
}

fun main(args: Array<String>) {
    var airports = DataLoader.fromFile(File(object{}.javaClass.getResource("/airports.txt").toURI()))

    val ksmo = airports.find { it.name.equals("KSMO") }
    airports = airports.filter { it.name != ksmo?.name }

    var definedStart = Point(lat = ksmo!!.point.lat, lon = ksmo!!.point.lon)
    val startLocation = Location.newInstance(definedStart.lat, definedStart.lon)

    // TODO: the time window for jsprit makes no sense to me yet.
    val validTime = TimeWindow.newInstance(0.0, 9.0)

    val vehicleType: VehicleTypeImpl = VehicleTypeImpl.Builder.newInstance("Airplane")
        .setMaxVelocity(knotsToMetersPerSecond(160.0))
        .addCapacityDimension(0, 200)
        .setCostPerDistance(1.0)
        .build()

    val vehicle: VehicleImpl = VehicleImpl.Builder.newInstance("RV-7")
        .setStartLocation(startLocation)
        .setType(vehicleType)
        .setUserData(definedStart)
        .setReturnToDepot(true)
        .setEarliestStart(validTime.start)
        .setLatestArrival(validTime.end)
        .build()

    val jobs = airports.map {  Pickup.Builder
        .newInstance(it.name)
        .setLocation(Location.newInstance(it.point.lat, it.point.lon))
        .setTimeWindow(validTime)
        .addSizeDimension(0, 1)
        .build() }

    val problem: VehicleRoutingProblem = VehicleRoutingProblem.Builder.newInstance()
        .addVehicle(vehicle)
        .setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE)
        .addAllJobs(jobs)
        .build()

    val algo: VehicleRoutingAlgorithm = SchrimpfFactory().createAlgorithm(problem)

    val solutions = algo.searchSolutions()
    val bestSolution = Solutions.bestOf(solutions)
    SolutionPrinter.print(bestSolution)

    val plotter: Plotter = Plotter(problem, bestSolution)
    plotter.setLabel(Label.ID)
    plotter.plot("output/solution.png", "Socal Sectional Route Plans")

}
