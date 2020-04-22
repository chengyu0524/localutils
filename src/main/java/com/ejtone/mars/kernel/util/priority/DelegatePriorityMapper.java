package com.ejtone.mars.kernel.util.priority;

public class DelegatePriorityMapper implements PriorityMapper {

	private PriorityMapper delegate = new DefaultPriorityMapper( );

	public static final DelegatePriorityMapper getInstance( ) {
		return InstanceHolder.instane;
	}

	private DelegatePriorityMapper( ) {

	}


	public void setDelegate( PriorityMapper delegate ) {
		this.delegate = delegate;
	}

	@Override
	public int convert( int value ) {
		return delegate.convert( value );
	}

	@Override
	public int safe( int priority ) {
		return delegate.safe( priority );
	}

	@Override
	public int maxPriority( ) {
		return delegate.maxPriority( );
	}

	private static class InstanceHolder {
		public static final DelegatePriorityMapper instane = new DelegatePriorityMapper( );
	}
}
