package com.bigdistributor.tasks.deconvolution;

import mpicbg.spim.data.generic.base.Entity;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.Illumination;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import net.preibisch.mvrecon.fiji.spimdata.SpimData2;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.constellation.grouping.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public enum FusionGroup {
                    ALL_VIEW_TOGETHER("All views together"),
                    ALL_VIEW("Each view"),
                    Each_Timepoint_AND_Channel("Each timepoint & channel"),
                    Each_Timepoint_Channel_Illumination("Each timepoint, channel & illumination");

    private final String message;

    FusionGroup(String message) {
        this.message = message;
    }

    public String getTitle( final Group< ViewDescription > group )
    {
        String title;
        final ViewDescription vd0 = group.iterator().next();

        if ( this == Each_Timepoint_AND_Channel ) // "Each timepoint & channel"
            title = "fused_tp_" + vd0.getTimePointId() + "_ch_" + vd0.getViewSetup().getChannel().getId();
        else if ( this == Each_Timepoint_Channel_Illumination ) // "Each timepoint, channel & illumination"
            title = "fused_tp_" + vd0.getTimePointId() + "_ch_" + vd0.getViewSetup().getChannel().getId() + "_illum_" + vd0.getViewSetup().getIllumination().getId();
        else if ( this == ALL_VIEW_TOGETHER) // "All views together"
            title = "fused";
        else // "All views"
            title = "fused_tp_" + vd0.getTimePointId() + "_vs_" + vd0.getViewSetupId();

        return title;
    }

    public static List<Group<ViewDescription>> getFusionGroups(final SpimData2 spimData, final List<ViewId> views, final FusionGroup grouping) {
        final ArrayList<ViewDescription> vds = SpimData2.getAllViewDescriptionsSorted(spimData, views);
        List<Group<ViewDescription>> grouped = new ArrayList<>();
        switch (grouping) {
            case ALL_VIEW: {
                for (final ViewDescription vd : vds)
                    grouped.add(new Group<>(vd));
                break;
            }
            case ALL_VIEW_TOGETHER: {
                final Group<ViewDescription> allViews = new Group<>(vds);
                grouped.add(allViews);
                break;
            }
            case Each_Timepoint_AND_Channel: {
                final HashSet<Class<? extends Entity>> groupingFactors = new HashSet<>();
                groupingFactors.add(TimePoint.class);
                groupingFactors.add(Channel.class);
                grouped = Group.splitBy(vds, groupingFactors);
                break;
            }
            case Each_Timepoint_Channel_Illumination: {
                final HashSet<Class<? extends Entity>> groupingFactors = new HashSet<>();
                groupingFactors.add(TimePoint.class);
                groupingFactors.add(Channel.class);
                groupingFactors.add(Illumination.class);
                grouped = Group.splitBy(vds, groupingFactors);

                break;
            }
        }
        return grouped;
    }
}
