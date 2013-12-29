package net.dhleong.acl.net.weap;

import net.dhleong.acl.enums.BeamFrequency;
import net.dhleong.acl.net.ShipActionPacket;

public class SetBeamFreqPacket extends ShipActionPacket {
    public SetBeamFreqPacket(BeamFrequency frequency) {
        super(TYPE_SET_BEAMFREQ, frequency != null ? frequency.ordinal(): -1);

        if (frequency == null) {
        	throw new IllegalArgumentException(
        			"You must specify a beam frequency"
        	);
        }
    }

	@Override
	protected void appendPacketDetail(StringBuilder b) {
		b.append(BeamFrequency.values()[mArg]);
	}
}